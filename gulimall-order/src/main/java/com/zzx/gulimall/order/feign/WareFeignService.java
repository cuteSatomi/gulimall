package com.zzx.gulimall.order.feign;

import com.zzx.common.utils.R;
import com.zzx.gulimall.order.vo.WareSkuLockVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

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

    @GetMapping("/ware/wareinfo/getFare")
    R getFare(@RequestParam("addrId") Long addrId);

    @PostMapping("/ware/waresku/order/lock")
    R orderLockStock(@RequestBody WareSkuLockVO vo);
}
