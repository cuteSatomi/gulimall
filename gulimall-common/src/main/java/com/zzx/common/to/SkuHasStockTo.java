package com.zzx.common.to;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

/**
 * @author zzx
 * @date 2021-05-20 15:19
 */
@Data
@ToString
public class SkuHasStockTo implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long skuId;
    private Boolean hasStock;
}
