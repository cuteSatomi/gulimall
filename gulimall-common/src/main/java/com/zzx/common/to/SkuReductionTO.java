package com.zzx.common.to;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 *
 * @author zzx
 * @date 2021-05-12 15:43:56
 */
@Data
public class SkuReductionTO {
    private Long skuId;
    private int fullCount;
    private BigDecimal discount;
    private int countStatus;
    private BigDecimal fullPrice;
    private BigDecimal reducePrice;
    private int priceStatus;
    private List<MemberPrice> memberPrice;
}
