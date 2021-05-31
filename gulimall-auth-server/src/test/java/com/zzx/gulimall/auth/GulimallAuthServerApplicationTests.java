package com.zzx.gulimall.auth;

import com.zzx.common.utils.R;
import com.zzx.gulimall.auth.feign.MemberFeignService;
import com.zzx.gulimall.auth.vo.UserRegisterVO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GulimallAuthServerApplicationTests {

    @Autowired
    private MemberFeignService memberFeignService;

    @Test
    public  void contextLoads() {
        UserRegisterVO vo = new UserRegisterVO();
        vo.setUsername("zzx");
        vo.setPassword("123");
        vo.setPhone("13757575849");
        R r = memberFeignService.register(vo);
        System.out.println(r);
    }

}
