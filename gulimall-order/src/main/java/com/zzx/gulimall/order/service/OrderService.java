package com.zzx.gulimall.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zzx.common.utils.PageUtils;
import com.zzx.gulimall.order.entity.OrderEntity;
import com.zzx.gulimall.order.vo.OrderConfirmVO;
import com.zzx.gulimall.order.vo.OrderSubmitResponseVO;
import com.zzx.gulimall.order.vo.OrderSubmitVO;

import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * 订单
 *
 * @author zzx
 * @email hdsomedezzx@gmail.com
 * @date 2021-05-03 23:22:16
 */
public interface OrderService extends IService<OrderEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 订单确认页返回需要的数据
     * @return
     */
    OrderConfirmVO confirmOrder() throws ExecutionException, InterruptedException;

    OrderSubmitResponseVO submitOrder(OrderSubmitVO vo);

    /**
     * 根据订单号查询订单
     * @param orderSn
     * @return
     */
    OrderEntity getOrderByOrderSn(String orderSn);

    /**
     * 关闭订单
     * @param entity
     */
    void closeOrder(OrderEntity entity);
}

