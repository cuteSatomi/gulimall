package com.zzx.gulimall.order.feign;

import com.zzx.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author zzx
 * @date 2021-06-11 14:04
 */
@FeignClient("gulimall-product")
public interface ProductFeignService {


    @GetMapping("/product/spuinfo/getSpuInfo/{skuId}")
    R getSpuInfoBySkuId(@PathVariable("skuId") Long skuId);
}
