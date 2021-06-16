package com.zzx.gulimall.coupon;

import com.zzx.gulimall.coupon.service.impl.SeckillSessionServiceImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
class GulimallCouponApplicationTests {

    @Test
    void contextLoads() {
    }

    public static void main(String[] args) {
        SeckillSessionServiceImpl seckillSessionService = new SeckillSessionServiceImpl();
        System.out.println(seckillSessionService.startTime());
        System.out.println(seckillSessionService.endTime());

    }

}
