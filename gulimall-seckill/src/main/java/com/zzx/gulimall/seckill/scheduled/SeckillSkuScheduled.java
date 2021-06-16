package com.zzx.gulimall.seckill.scheduled;

import com.zzx.gulimall.seckill.service.SeckillService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 *
 * @author zzx
 * @date 2021-06-16 21:20
 */
@Slf4j
@Service
public class SeckillSkuScheduled {

    @Autowired
    private SeckillService seckillService;

    /**
     * 每晚三点上架最近三天的秒杀商品
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void uploadSeckillSkuLatest3Days(){
        seckillService.uploadSeckillSkuLatest3Days();
    }
}
