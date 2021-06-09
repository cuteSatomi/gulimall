package com.zzx.gulimall.order.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 订单确认页需要用的数据
 *
 * @author zzx
 * @date 2021-06-09 16:51
 */
@Data
public class OrderConfirmVO {
    /** 收货地址，ums_member_receive_address */
    private List<MemberAddressVO> address;

    /** 所有选中的购物项 */
    private List<OrderItemVO> items;

    // TODO 发票。。。

    /** 优惠券 */
    private Integer integration;

    /** 订单总额 */
    private BigDecimal total;

    /** 应付总额 */
    private BigDecimal payPrice;
}
