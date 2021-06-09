package com.zzx.gulimall.cart.feign;

import com.zzx.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author zzx
 * @date 2021-06-06 15:31
 */
@FeignClient("gulimall-product")
public interface ProductFeignService {
    /**
     * 调用product获取sku基本信息
     *
     * @param skuId
     * @return
     */
    @RequestMapping("/product/skuinfo/info/{skuId}")
    R getSkuInfo(@PathVariable("skuId") Long skuId);

    /**
     * 根据skuId获取该商品的销售属性，拼装成list集合
     *
     * @param skuId
     * @return
     */
    @GetMapping("/product/skusaleattrvalue/stringlist/{skuId}")
    List<String> getSkuSaleAttrValueAsStringList(@PathVariable Long skuId);

    /**
     * 查询商品的最新价格
     *
     * @param skuId
     * @return
     */
    @GetMapping("/product/skuinfo/{skuId}/newPrice")
    BigDecimal getNewPrice(@PathVariable("skuId") Long skuId);
}
