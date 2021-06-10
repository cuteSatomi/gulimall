package com.zzx.gulimall.order.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zzx.common.utils.PageUtils;
import com.zzx.common.utils.Query;
import com.zzx.common.utils.R;
import com.zzx.common.vo.MemberResponseVO;
import com.zzx.gulimall.order.constant.OrderConstant;
import com.zzx.gulimall.order.dao.OrderDao;
import com.zzx.gulimall.order.entity.OrderEntity;
import com.zzx.gulimall.order.feign.CartFeignService;
import com.zzx.gulimall.order.feign.MemberFeignService;
import com.zzx.gulimall.order.feign.WareFeignService;
import com.zzx.gulimall.order.interceptor.LoginUserInterceptor;
import com.zzx.gulimall.order.service.OrderService;
import com.zzx.gulimall.order.vo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

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

    @Override
    public OrderSubmitResponseVO submitOrder(OrderSubmitVO vo) {
        OrderSubmitResponseVO response = new OrderSubmitResponseVO();
        MemberResponseVO memberResponseVO = LoginUserInterceptor.loginUser.get();
        // 验证令牌，【令牌的对比和删除必须保证原子性】
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        String orderToken = vo.getOrderToken();
        // 原子验证令牌和删除令牌
        Long result = stringRedisTemplate.execute(new DefaultRedisScript<Long>(script, Long.class),
                Arrays.asList(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberResponseVO.getId()), orderToken);
        if (result == 0L) {
            // 令牌验证失败
            return response;
        } else {
            // 令牌验证成功
        }

        return null;
    }

}