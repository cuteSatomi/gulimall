package com.zzx.gulimall.ware.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author zzx
 * @date 2021-06-09 16:56
 */
@Data
public class OrderItemVO {
    private Long skuId;
    private Boolean check;
    private String title;
    private String image;
    private List<String> skuAttr;
    private BigDecimal price;
    private Integer count;
    private BigDecimal totalPrice;

    private BigDecimal weight;
}
