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
     * ????????????????????????????????????
     *
     * @return
     */
    @Override
    public OrderConfirmVO confirmOrder() throws ExecutionException, InterruptedException {
        MemberResponseVO member = LoginUserInterceptor.loginUser.get();

        OrderConfirmVO confirmVO = new OrderConfirmVO();

        // ????????????????????????????????????
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();

        CompletableFuture<Void> getAddressesTask = CompletableFuture.runAsync(() -> {
            // ?????????????????????????????????????????????????????????????????????????????????????????????
            RequestContextHolder.setRequestAttributes(requestAttributes);
            // 1???????????????????????????????????????
            List<MemberAddressVO> addresses = memberFeignService.getAddresses(member.getId());
            confirmVO.setAddress(addresses);
        }, executor);

        CompletableFuture<Void> getCartItemsTask = CompletableFuture.runAsync(() -> {
            // ?????????????????????????????????????????????????????????????????????????????????????????????
            RequestContextHolder.setRequestAttributes(requestAttributes);
            // 2???????????????????????????????????????
            List<OrderItemVO> items = cartFeignService.getCurrentUserCartItems();
            confirmVO.setItems(items);
        }, executor).thenRunAsync(() -> {
            List<OrderItemVO> items = confirmVO.getItems();
            // ??????skuId
            List<Long> skuIds = items.stream().map(OrderItemVO::getSkuId).collect(Collectors.toList());
            R hasStock = wareFeignService.getSkusHasStock(skuIds);
            List<SkuStockVO> data = hasStock.getData(new TypeReference<List<SkuStockVO>>() {
            });
            if (data != null && data.size() > 0) {
                Map<Long, Boolean> collect = data.stream().collect(Collectors.toMap(SkuStockVO::getSkuId, SkuStockVO::getHasStock));
                confirmVO.setStocks(collect);
            }
        }, executor);

        // 3?????????????????????
        confirmVO.setIntegration(member.getIntegration());

        // 4???????????????????????????

        // 5???????????????
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

        // ??????????????????????????????????????????????????????????????????
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        String orderToken = vo.getOrderToken();
        // ?????????????????????????????????
        Long result = stringRedisTemplate.execute(new DefaultRedisScript<Long>(script, Long.class),
                Arrays.asList(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberResponseVO.getId()), orderToken);
        if (result == 0L) {
            // ??????????????????
            response.setCode(1);
            return response;
        } else {
            // ?????????????????????????????????
            // 1???????????????
            OrderCreateTo order = createOrder(vo);
            // 2?????????
            BigDecimal payAmount = order.getOrder().getPayAmount();
            BigDecimal payPrice = vo.getPayPrice();
            if (Math.abs(payAmount.subtract(payAmount).doubleValue()) < 0.01) {
                // ????????????
                // ????????????
                saveOrder(order);

                // ????????????
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
                    // ??????????????????
                    response.setOrder(order.getOrder());
                    // ??????????????????????????????????????????MQ????????????
                    rabbitTemplate.convertAndSend("order-event-exchange", "order.create.order", order.getOrder());
                    return response;
                } else {
                    // ????????????
                    throw new RuntimeException("??????????????????");
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
     * ????????????
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

            // ????????????????????????mq???????????????
            OrderTo orderTo = new OrderTo();
            BeanUtils.copyProperties(orderEntity,orderTo);
            rabbitTemplate.convertAndSend("order-event-exchange", "order.release.other", orderTo);
        }
    }

    /**
     * ????????????
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
     * ????????????
     *
     * @param vo
     * @return
     */
    private OrderCreateTo createOrder(OrderSubmitVO vo) {
        OrderCreateTo orderCreateTo = new OrderCreateTo();
        // 1??????????????????
        String orderSn = IdWorker.getTimeId();
        OrderEntity order = buildOrder(vo, orderSn);

        // ?????????????????????
        List<OrderItemEntity> orderItems = buildOrderItems(orderSn);

        // ??????????????????
        computePrice(order, orderItems);

        orderCreateTo.setOrder(order);
        orderCreateTo.setOrderItems(orderItems);

        return orderCreateTo;
    }

    /**
     * ??????????????????
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
        // ????????????
        order.setPayAmount(total.add(order.getFreightAmount()));
        order.setCouponAmount(coupon);
        order.setIntegrationAmount(integration);
        order.setPromotionAmount(promotion);

        // ??????????????????
        order.setIntegration(giftIntegration);
        order.setGrowth(giftGrowth);

        order.setDeleteStatus(0);
    }

    /**
     * ?????????????????????
     *
     * @param orderSn
     * @return
     */
    private List<OrderItemEntity> buildOrderItems(String orderSn) {
        List<OrderItemVO> currentUserCartItems = cartFeignService.getCurrentUserCartItems();
        if (currentUserCartItems != null && currentUserCartItems.size() > 0) {
            List<OrderItemEntity> orderItems = currentUserCartItems.stream().map(cartItem -> {
                OrderItemEntity orderItem = buildOrderItem(cartItem);
                // ?????????????????????????????????
                orderItem.setOrderSn(orderSn);

                return orderItem;
            }).collect(Collectors.toList());
            return orderItems;
        }
        return null;
    }

    /**
     * ????????????????????????
     *
     * @param cartItem
     * @return
     */
    private OrderItemEntity buildOrderItem(OrderItemVO cartItem) {
        OrderItemEntity orderItemEntity = new OrderItemEntity();

        // 1????????????spu??????
        Long skuId = cartItem.getSkuId();
        R data = productFeignService.getSpuInfoBySkuId(skuId);
        SpuInfoVO spuInfo = data.getData(new TypeReference<SpuInfoVO>() {
        });
        orderItemEntity.setSpuBrand(spuInfo.getBrandId().toString());
        orderItemEntity.setSpuId(spuInfo.getId());
        orderItemEntity.setSpuName(spuInfo.getSpuName());
        orderItemEntity.setCategoryId(spuInfo.getCatalogId());

        // 2????????????sku??????
        orderItemEntity.setSkuId(skuId);
        orderItemEntity.setSkuName(cartItem.getTitle());
        orderItemEntity.setSkuPic(cartItem.getImage());
        orderItemEntity.setSkuPrice(cartItem.getPrice());
        String skuAttrs = StringUtils.collectionToDelimitedString(cartItem.getSkuAttr(), ";");
        orderItemEntity.setSkuAttrsVals(skuAttrs);
        orderItemEntity.setSkuQuantity(cartItem.getCount());

        // 3????????????????????????????????????
        // 4??????????????????????????????
        orderItemEntity.setGiftGrowth(cartItem.getPrice().multiply(new BigDecimal(cartItem.getCount().toString())).intValue());
        orderItemEntity.setGiftIntegration(cartItem.getPrice().multiply(new BigDecimal(cartItem.getCount().toString())).intValue());

        // 5???????????????
        orderItemEntity.setIntegrationAmount(new BigDecimal("0"));
        orderItemEntity.setCouponAmount(new BigDecimal("0"));
        orderItemEntity.setPromotionAmount(new BigDecimal("0"));
        BigDecimal originPrice = orderItemEntity.getSkuPrice().multiply(new BigDecimal(orderItemEntity.getSkuQuantity().toString()));
        BigDecimal subtract = originPrice.subtract(orderItemEntity.getIntegrationAmount()).subtract(orderItemEntity.getCouponAmount()).subtract(orderItemEntity.getPromotionAmount());
        orderItemEntity.setRealAmount(subtract);

        return orderItemEntity;
    }

    /**
     * ????????????
     *
     * @param vo
     * @param orderSn
     * @return
     */
    private OrderEntity buildOrder(OrderSubmitVO vo, String orderSn) {
        MemberResponseVO memberResponseVO = LoginUserInterceptor.loginUser.get();
        OrderEntity entity = new OrderEntity();
        entity.setOrderSn(orderSn);

        // ????????????id
        entity.setMemberId(memberResponseVO.getId());

        R fare = wareFeignService.getFare(vo.getAddrId());
        FareVO fareResp = fare.getData(new TypeReference<FareVO>() {
        });

        // ??????????????????
        entity.setFreightAmount(fareResp.getFare());

        // ?????????????????????
        entity.setReceiverCity(fareResp.getAddress().getCity());
        entity.setReceiverName(fareResp.getAddress().getName());
        entity.setReceiverDetailAddress(fareResp.getAddress().getDetailAddress());
        entity.setReceiverPhone(fareResp.getAddress().getPhone());
        entity.setReceiverProvince(fareResp.getAddress().getProvince());
        entity.setReceiverPostCode(fareResp.getAddress().getPostCode());
        entity.setReceiverRegion(fareResp.getAddress().getRegion());

        // ??????????????????
        entity.setStatus(OrderStatusEnum.CREATE_NEW.getCode());
        entity.setAutoConfirmDay(7);

        return entity;
    }

}