package com.zzx.gulimall.cart.vo;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author zzx
 * @date 2021-06-04 20:55
 */
@Data
public class CartItem {
    private Long skuId;
    private Boolean check = true;
    private String title;
    private String image;
    private List<String> skuAttr;
    private BigDecimal price;
    private Integer count;
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private BigDecimal totalPrice;

    /**
     * 手动计算总价
     * @return
     */
    public BigDecimal getTotalPrice() {
        return this.price.multiply(BigDecimal.valueOf(this.count));
    }
}
