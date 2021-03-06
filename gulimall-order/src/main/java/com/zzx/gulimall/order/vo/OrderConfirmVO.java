package com.zzx.gulimall.order.vo;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

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

    private Map<Long, Boolean> stocks;

    /** 防重令牌 */
    private String orderToken;

    /** 订单总额 */
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private BigDecimal total;

    /** 应付总额 */
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private BigDecimal payPrice;

    public Integer getCount() {
        Integer count = 0;
        if (items != null && items.size() > 0) {
            for (OrderItemVO item : items) {
                count += item.getCount();
            }
        }
        return count;
    }

    public BigDecimal getTotal() {
        BigDecimal sum = new BigDecimal("0");
        if (items != null && items.size() > 0) {
            for (OrderItemVO item : items) {
                BigDecimal multiply = item.getPrice().multiply(BigDecimal.valueOf(item.getCount()));
                sum = sum.add(multiply);
            }
        }
        return sum;
    }

    public BigDecimal getPayPrice() {
        return getTotal();
    }
}
