package com.zzx.gulimall.cart.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author zzx
 * @date 2021-06-04 20:54
 */
@Data
public class Cart {
    List<CartItem> items;
    /** 商品总数量 */
    private Integer countNum;
    /** 商品类型数量 */
    private Integer countType;
    /** 商品总价 */
    private BigDecimal totalAmount;
    /** 优惠价格 */
    private BigDecimal reduce = new BigDecimal("0.00");

    public List<CartItem> getItems() {
        return items;
    }

    public void setItems(List<CartItem> items) {
        this.items = items;
    }

    public Integer getCountNum() {
        int count = 0;
        if (items != null && items.size() > 0) {
            for (CartItem item : items) {
                count += item.getCount();
            }
        }
        return count;
    }

    public Integer getCountType() {
        return items == null ? 0 : items.size();
    }

    public BigDecimal getTotalAmount() {
        BigDecimal amount = new BigDecimal("0.00");
        // 计算购物项总价
        if (items != null && items.size() > 0) {
            for (CartItem item : items) {
                if(item.getCheck()){
                    amount = amount.add(item.getTotalPrice());
                }
            }
        }
        // 减去优惠的价格
        return amount.subtract(reduce);
    }

    public BigDecimal getReduce() {
        return reduce;
    }
}
