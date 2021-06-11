package com.zzx.gulimall.ware.exception;

/**
 * @author zzx
 * @date 2021-06-11 15:56
 */
public class NoStockException extends RuntimeException {
    private Long skuId;

    public NoStockException(Long skuId) {
        super("商品：" + skuId + "；库存不足，锁定库存失败");
    }

    public Long getSkuId() {
        return skuId;
    }

    public void setSkuId(Long skuId) {
        this.skuId = skuId;
    }
}
