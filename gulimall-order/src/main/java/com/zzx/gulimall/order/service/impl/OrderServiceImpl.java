package com.zzx.gulimall.order.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zzx.common.to.mq.OrderTo;
import com.zzx.common.utils.PageUtils;
import com.zzx.common.utils.Query;
import com.zzx.common.utils.R;
import com.zzx.common.vo.MemberResponseVO;
import com.zzx.gulimall.order.constant.OrderConstant;
import com.zzx.gulimall.order.dao.OrderDao;
import com.zzx.gulimall.order.entity.OrderEntity;
import com.zzx.gulimall.order.entity.OrderItemEntity;
import com.zzx.gulimall.order.enume.OrderStatusEnum;
import com.zzx.gulimall.order.feign.CartFeignService;
import com.zzx.gulimall.order.feign.MemberFeignService;
import com.zzx.gulimall.order.feign.ProductFeignService;
import com.zzx.gulimall.order.feign.WareFeignService;
import com.zzx.gulimall.order.interceptor.LoginUserInterceptor;
import com.zzx.gulimall.order.service.OrderItemService;
import com.zzx.gulimall.order.service.OrderService;
import com.zzx.gulimall.order.to.OrderCreateTo;
import com.zzx.gulimall.order.vo.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {

    @Autowired
    private MemberFeignService memberFeignService;

    @Autowired
    private CartFeignService cartFeignService;

    @Autowired
    private ThreadPoolExecutor executor;

    @Autowired
    private WareFeignService wareFeignService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private ProductFeignService productFeignService;

    @Autowired
    private OrderItemService orderItemService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 订单确认页返回需要的数据
     *
     * @return
     */
    @Override
    public OrderConfirmVO confirmOrder() throws ExecutionException, InterruptedException {
        MemberResponseVO member = LoginUserInterceptor.loginUser.get();

        OrderConfirmVO confirmVO = new OrderConfirmVO();

        // 获取主线程中的上下文对象
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();

        CompletableFuture<Void> getAddressesTask = CompletableFuture.runAsync(() -> {
            // 为异步任务的线程设置相同的上下文对象，让他们可以拿到请求头数据
            RequestContextHolder.setRequestAttributes(requestAttributes);
            // 1、远程查询所有收获地址列表
            List<MemberAddressVO> addresses = memberFeignService.getAddresses(member.getId());
            confirmVO.setAddress(addresses);
        }, executor);

        CompletableFuture<Void> getCartItemsTask = CompletableFuture.runAsync(() -> {
            // 为异步任务的线程设置相同的上下文对象，让他们可以拿到请求头数据
            RequestContextHolder.setRequestAttributes(requestAttributes);
            // 2、远程查询购物车所有选中项
            List<OrderItemVO> items = cartFeignService.getCurrentUserCartItems();
            confirmVO.setItems(items);
        }, executor).thenRunAsync(() -> {
            List<OrderItemVO> items = confirmVO.getItems();
            // 获取skuId
            List<Long> skuIds = items.stream().map(OrderItemVO::getSkuId).collect(Collectors.toList());
            R hasStock = wareFeignService.getSkusHasStock(skuIds);
            List<SkuStockVO> data = hasStock.getData(new TypeReference<List<SkuStockVO>>() {
            });
            if (data != null && data.size() > 0) {
                Map<Long, Boolean> collect = data.stream().collect(Collectors.toMap(SkuStockVO::getSkuId, SkuStockVO::getHasStock));
                confirmVO.setStocks(collect);
            }
        }, executor);

        // 3、查询用户积分
        confirmVO.setIntegration(member.getIntegration());

        // 4、其他数据自动计算

        // 5、防重令牌
        String token = UUID.randomUUID().toString().replace("-", "");
        stringRedisTemplate.opsForValue().set(OrderConstant.USER_ORDER_TOKEN_PREFIX + member.getId(), token, 30, TimeUnit.MINUTES);
        confirmVO.setOrderToken(token);

        CompletableFuture.allOf(getAddressesTask, getCartItemsTask).get();

        return confirmVO;
    }

    @Transactional
    @Override
    public OrderSubmitResponseVO submitOrder(OrderSubmitVO vo) {
        OrderSubmitResponseVO response = new OrderSubmitResponseVO();
        MemberResponseVO memberResponseVO = LoginUserInterceptor.loginUser.get();
        response.setCode(0);

        // 验证令牌，【令牌的对比和删除必须保证原子性】
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        String orderToken = vo.getOrderToken();
        // 原子验证令牌和删除令牌
        Long result = stringRedisTemplate.execute(new DefaultRedisScript<Long>(script, Long.class),
                Arrays.asList(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberResponseVO.getId()), orderToken);
        if (result == 0L) {
            // 令牌验证失败
            response.setCode(1);
            return response;
        } else {
            // 令牌验证成功，进行下单
            // 1、创建订单
            OrderCreateTo order = createOrder(vo);
            // 2、验价
            BigDecimal payAmount = order.getOrder().getPayAmount();
            BigDecimal payPrice = vo.getPayPrice();
            if (Math.abs(payAmount.subtract(payAmount).doubleValue()) < 0.01) {
                // 验价成功
                // 保存订单
                saveOrder(order);

                // 库存锁定
                WareSkuLockVO lockVO = new WareSkuLockVO();
                lockVO.setOrderSn(order.getOrder().getOrderSn());
                List<OrderItemVO> locks = order.getOrderItems().stream().map(orderItem -> {
                    OrderItemVO orderItemVO = new OrderItemVO();
                    orderItemVO.setSkuId(orderItem.getSkuId());
                    orderItemVO.setCount(orderItem.getSkuQuantity());
                    orderItemVO.setTitle(orderItem.getSkuName());
                    return orderItemVO;
                }).collect(Collectors.toList());
                lockVO.setLocks(locks);
                R r = wareFeignService.orderLockStock(lockVO);
                if (r.getCode() == 0) {
                    // 锁定库存成功
                    response.setOrder(order.getOrder());
                    // 订单创建已经库存锁定成功，向MQ发送消息
                    rabbitTemplate.convertAndSend("order-event-exchange", "order.create.order", order.getOrder());
                    return response;
                } else {
                    // 锁定失败
                    throw new RuntimeException("锁定库存失败");
                    /*response.setCode(3);
                    return response;*/
                }

            } else {
                response.setCode(2);
                return response;
            }
        }
    }

    @Override
    public OrderEntity getOrderByOrderSn(String orderSn) {
        return baseMapper.selectOne(new QueryWrapper<OrderEntity>().eq("order_sn", orderSn));
    }

    /**
     * 关闭订单
     *
     * @param entity
     */
    @Override
    public void closeOrder(OrderEntity entity) {
        OrderEntity orderEntity = this.getById(entity.getId());
        if (OrderStatusEnum.CREATE_NEW.getCode().equals(orderEntity.getStatus())) {
            OrderEntity update = new OrderEntity();
            update.setId(entity.getId());
            update.setStatus(OrderStatusEnum.CANCELED.getCode());
            this.updateById(update);

            // 取消订单成功再给mq发一个消息
            OrderTo orderTo = new OrderTo();
            BeanUtils.copyProperties(orderEntity,orderTo);
            rabbitTemplate.convertAndSend("order-event-exchange", "order.release.other", orderTo);
        }
    }

    /**
     * 保存订单
     *
     * @param order
     */
    private void saveOrder(OrderCreateTo order) {
        OrderEntity orderEntity = order.getOrder();
        this.save(orderEntity);

        List<OrderItemEntity> orderItems = order.getOrderItems();
        orderItemService.saveBatch(orderItems);
    }

    /**
     * 创建订单
     *
     * @param vo
     * @return
     */
    private OrderCreateTo createOrder(OrderSubmitVO vo) {
        OrderCreateTo orderCreateTo = new OrderCreateTo();
        // 1、生成订单号
        String orderSn = IdWorker.getTimeId();
        OrderEntity order = buildOrder(vo, orderSn);

        // 构建所有订单项
        List<OrderItemEntity> orderItems = buildOrderItems(orderSn);

        // 计算价格相关
        computePrice(order, orderItems);

        orderCreateTo.setOrder(order);
        orderCreateTo.setOrderItems(orderItems);

        return orderCreateTo;
    }

    /**
     * 计算价格相关
     *
     * @param order
     * @param orderItems
     */
    private void computePrice(OrderEntity order, List<OrderItemEntity> orderItems) {
        BigDecimal total = new BigDecimal("0");
        BigDecimal coupon = new BigDecimal("0");
        BigDecimal integration = new BigDecimal("0");
        BigDecimal promotion = new BigDecimal("0");
        Integer giftGrowth = 0;
        Integer giftIntegration = 0;

        for (OrderItemEntity item : orderItems) {
            total = total.add(item.getRealAmount());
            coupon = coupon.add(item.getCouponAmount());
            integration = integration.add(item.getIntegrationAmount());
            promotion = promotion.add(item.getPromotionAmount());
            giftGrowth += item.getGiftGrowth();
            giftIntegration += item.getGiftIntegration();
        }

        order.setTotalAmount(total);
        // 应付总额
        order.setPayAmount(total.add(order.getFreightAmount()));
        order.setCouponAmount(coupon);
        order.setIntegrationAmount(integration);
        order.setPromotionAmount(promotion);

        // 设置积分信息
        order.setIntegration(giftIntegration);
        order.setGrowth(giftGrowth);

        order.setDeleteStatus(0);
    }

    /**
     * 构建所有订单项
     *
     * @param orderSn
     * @return
     */
    private List<OrderItemEntity> buildOrderItems(String orderSn) {
        List<OrderItemVO> currentUserCartItems = cartFeignService.getCurrentUserCartItems();
        if (currentUserCartItems != null && currentUserCartItems.size() > 0) {
            List<OrderItemEntity> orderItems = currentUserCartItems.stream().map(cartItem -> {
                OrderItemEntity orderItem = buildOrderItem(cartItem);
                // 订单信息，主要是订单号
                orderItem.setOrderSn(orderSn);

                return orderItem;
            }).collect(Collectors.toList());
            return orderItems;
        }
        return null;
    }

    /**
     * 构建某一个订单项
     *
     * @param cartItem
     * @return
     */
    private OrderItemEntity buildOrderItem(OrderItemVO cartItem) {
        OrderItemEntity orderItemEntity = new OrderItemEntity();

        // 1、商品的spu信息
        Long skuId = cartItem.getSkuId();
        R data = productFeignService.getSpuInfoBySkuId(skuId);
        SpuInfoVO spuInfo = data.getData(new TypeReference<SpuInfoVO>() {
        });
        orderItemEntity.setSpuBrand(spuInfo.getBrandId().toString());
        orderItemEntity.setSpuId(spuInfo.getId());
        orderItemEntity.setSpuName(spuInfo.getSpuName());
        orderItemEntity.setCategoryId(spuInfo.getCatalogId());

        // 2、商品的sku信息
        orderItemEntity.setSkuId(skuId);
        orderItemEntity.setSkuName(cartItem.getTitle());
        orderItemEntity.setSkuPic(cartItem.getImage());
        orderItemEntity.setSkuPrice(cartItem.getPrice());
        String skuAttrs = StringUtils.collectionToDelimitedString(cartItem.getSkuAttr(), ";");
        orderItemEntity.setSkuAttrsVals(skuAttrs);
        orderItemEntity.setSkuQuantity(cartItem.getCount());

        // 3、商品的优惠信息【不做】
        // 4、购买成功的积分信息
        orderItemEntity.setGiftGrowth(cartItem.getPrice().multiply(new BigDecimal(cartItem.getCount().toString())).intValue());
        orderItemEntity.setGiftIntegration(cartItem.getPrice().multiply(new BigDecimal(cartItem.getCount().toString())).intValue());

        // 5、价格信息
        orderItemEntity.setIntegrationAmount(new BigDecimal("0"));
        orderItemEntity.setCouponAmount(new BigDecimal("0"));
        orderItemEntity.setPromotionAmount(new BigDecimal("0"));
        BigDecimal originPrice = orderItemEntity.getSkuPrice().multiply(new BigDecimal(orderItemEntity.getSkuQuantity().toString()));
        BigDecimal subtract = originPrice.subtract(orderItemEntity.getIntegrationAmount()).subtract(orderItemEntity.getCouponAmount()).subtract(orderItemEntity.getPromotionAmount());
        orderItemEntity.setRealAmount(subtract);

        return orderItemEntity;
    }

    /**
     * 构建订单
     *
     * @param vo
     * @param orderSn
     * @return
     */
    private OrderEntity buildOrder(OrderSubmitVO vo, String orderSn) {
        MemberResponseVO memberResponseVO = LoginUserInterceptor.loginUser.get();
        OrderEntity entity = new OrderEntity();
        entity.setOrderSn(orderSn);

        // 设置会员id
        entity.setMemberId(memberResponseVO.getId());

        R fare = wareFeignService.getFare(vo.getAddrId());
        FareVO fareResp = fare.getData(new TypeReference<FareVO>() {
        });

        // 设置订单运费
        entity.setFreightAmount(fareResp.getFare());

        // 设置收货人信息
        entity.setReceiverCity(fareResp.getAddress().getCity());
        entity.setReceiverName(fareResp.getAddress().getName());
        entity.setReceiverDetailAddress(fareResp.getAddress().getDetailAddress());
        entity.setReceiverPhone(fareResp.getAddress().getPhone());
        entity.setReceiverProvince(fareResp.getAddress().getProvince());
        entity.setReceiverPostCode(fareResp.getAddress().getPostCode());
        entity.setReceiverRegion(fareResp.getAddress().getRegion());

        // 设置订单状态
        entity.setStatus(OrderStatusEnum.CREATE_NEW.getCode());
        entity.setAutoConfirmDay(7);

        return entity;
    }

}