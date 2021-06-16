package com.zzx.gulimall.seckill.scheduled;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author zzx
 * @date 2021-06-16 20:48
 */
@Slf4j
@Component
public class HelloSchedule {

    //@Scheduled(cron = "* * * ? * 3")
    public void hello(){
        log.info("hello...");
    }
}
