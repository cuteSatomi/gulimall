package com.zzx.gulimall.product.feign;

import com.zzx.common.to.SkuReductionTo;
import com.zzx.common.to.SpuBoundTo;
import com.zzx.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @author zzx
 * @date 2021-05-12 10:24:24
 */
@FeignClient("gulimall-coupon")
public interface CouponFeignService {

    /**
     * 保存spu的积分信息
     *
     * @param spuBoundTO
     */
    @PostMapping("/coupon/spubounds/save")
    R saveBounds(@RequestBody SpuBoundTo spuBoundTO);

    /**
     * 保存sku的优惠，满减等信息
     *
     * @param skuReductionTO
     */
    @PostMapping("/coupon/skufullreduction/saveInfo")
    R saveSkuReduction(@RequestBody SkuReductionTo skuReductionTO);
}
