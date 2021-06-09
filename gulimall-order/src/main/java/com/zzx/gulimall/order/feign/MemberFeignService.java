package com.zzx.gulimall.order.feign;

import com.zzx.gulimall.order.vo.MemberAddressVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * @author zzx
 * @date 2021-06-09 20:04
 */
@FeignClient("gulimall-member")
public interface MemberFeignService {

    /**
     * 根据会员id查询出该会员所有的收货地址
     *
     * @param memberId
     * @return
     */
    @GetMapping("/member/memberreceiveaddress/{memberId}/addresses")
    List<MemberAddressVO> getAddresses(@PathVariable("memberId") Long memberId);
}
