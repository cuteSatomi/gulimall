package com.zzx.gulimall.order.feign;

import com.zzx.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @author zzx
 * @date 2021-06-10 11:16
 */
@FeignClient("gulimall-ware")
public interface WareFeignService {

    /**
     * 查询sku是否有库存，供商品微服务远程调用
     *
     * @param skuIds
     * @return
     */
    @PostMapping("/ware/waresku/hasstock")
    R getSkusHasStock(@RequestBody List<Long> skuIds);
}
