package com.zzx.gulimall.order.to;

import com.zzx.gulimall.order.entity.OrderEntity;
import com.zzx.gulimall.order.entity.OrderItemEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author zzx
 * @date 2021-06-11 10:28
 */
@Data
public class OrderCreateTo {
    private OrderEntity order;
    private List<OrderItemEntity> orderItems;
    private BigDecimal payPrice;
    private BigDecimal fare;

}
