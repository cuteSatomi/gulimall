package com.zzx.gulimall.order.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author zzx
 * @date 2021-06-10 16:04
 */
@Data
public class OrderSubmitVO {
    /** 收获地址id */
    private Long addrId;
    /** 支付方式 */
    private Integer payType;
    // 无需提交需要购买的商品，去购物车再获取一遍勾选的商品

    /** 防重令牌 */
    private String orderToken;
    /** 应付价格 */
    private BigDecimal payPrice;
    /** 订单备注 */
    private String note;
    
}
