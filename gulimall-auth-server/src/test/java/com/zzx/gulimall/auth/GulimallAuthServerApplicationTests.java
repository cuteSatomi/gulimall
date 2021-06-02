package com.zzx.gulimall.auth;

import com.zzx.gulimall.auth.feign.MemberFeignService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GulimallAuthServerApplicationTests {

    @Autowired
    private MemberFeignService memberFeignService;

    @Value("${spring.cloud.login.weibo.client-secret}")
    private String secret;

    @Test
    public void contextLoads() {
        System.out.println(secret);
    }

}
