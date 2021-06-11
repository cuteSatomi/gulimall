package com.zzx.gulimall.ware.vo;

import lombok.Data;

/**
 * @author zzx
 * @date 2021-06-11 15:15
 */
@Data
public class LockStockResult {
    private Long skuId;
    private Integer num;
    private Boolean locked;
}
