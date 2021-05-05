package com.zzx.gulimall.member.client;

import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author zzx
 * @date 2021-05-04 13:27
 */
@FeignClient("gulimall-coupon")
public interface CouponClient {
}
