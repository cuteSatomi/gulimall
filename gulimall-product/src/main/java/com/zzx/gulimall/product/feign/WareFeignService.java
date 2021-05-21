package com.zzx.gulimall.product.feign;

import com.zzx.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @author zzx
 * @date 2021-05-20 15:28
 */
@FeignClient("gulimall-ware")
public interface WareFeignService {
    /**
     * 查询sku是否有库存
     *
     * @param skuIds
     * @return
     */
    @PostMapping("/ware/waresku/hasstock")
    R getSkusHasStock(@RequestBody List<Long> skuIds);
}
