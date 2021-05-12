package com.zzx.gulimall.product.feign;

import com.zzx.common.to.SkuReductionTO;
import com.zzx.common.to.SpuBoundTO;
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
    R saveBounds(@RequestBody SpuBoundTO spuBoundTO);

    /**
     * 保存sku的优惠，满减等信息
     *
     * @param skuReductionTO
     */
    @PostMapping("/coupon/skufullreduction/saveInfo")
    R saveSkuReduction(@RequestBody SkuReductionTO skuReductionTO);
}
