package com.zzx.gulimall.seckill.service;

/**
 * @author zzx
 * @date 2021-06-16 21:23
 */
public interface SeckillService {
    /**
     * 上架最近三天的秒杀商品
     */
    void uploadSeckillSkuLatest3Days();
}
