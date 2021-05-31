package com.zzx.gulimall.auth.feign;

import com.zzx.common.utils.R;
import com.zzx.gulimall.auth.vo.UserRegisterVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @author zzx
 * @date 2021-05-31 21:31
 */
@FeignClient("gulimall-member")
public interface MemberFeignService {
    @PostMapping("/member/member/register")
    R register(@RequestBody UserRegisterVO registerVo);
}
