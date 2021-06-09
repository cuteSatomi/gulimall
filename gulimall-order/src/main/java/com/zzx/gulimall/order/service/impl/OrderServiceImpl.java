package com.zzx.gulimall.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zzx.common.utils.PageUtils;
import com.zzx.common.utils.Query;
import com.zzx.common.vo.MemberResponseVO;
import com.zzx.gulimall.order.dao.OrderDao;
import com.zzx.gulimall.order.entity.OrderEntity;
import com.zzx.gulimall.order.feign.CartFeignService;
import com.zzx.gulimall.order.feign.MemberFeignService;
import com.zzx.gulimall.order.interceptor.LoginUserInterceptor;
import com.zzx.gulimall.order.service.OrderService;
import com.zzx.gulimall.order.vo.MemberAddressVO;
import com.zzx.gulimall.order.vo.OrderConfirmVO;
import com.zzx.gulimall.order.vo.OrderItemVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;


@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {

    @Autowired
    private MemberFeignService memberFeignService;

    @Autowired
    private CartFeignService cartFeignService;

    @Autowired
    private ThreadPoolExecutor executor;

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
        }, executor);

        // 3、查询用户积分
        confirmVO.setIntegration(member.getIntegration());

        // 4、其他数据自动计算

        // TODO 5、防重令牌

        CompletableFuture.allOf(getAddressesTask,getCartItemsTask).get();

        return confirmVO;
    }

}