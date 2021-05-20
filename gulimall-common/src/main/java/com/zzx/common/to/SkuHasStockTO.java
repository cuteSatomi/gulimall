package com.zzx.common.to;

import lombok.Data;

/**
 * @author zzx
 * @date 2021-05-20 15:19
 */
@Data
public class SkuHasStockTO {
    private Long skuId;
    private Boolean hasStock;
}
