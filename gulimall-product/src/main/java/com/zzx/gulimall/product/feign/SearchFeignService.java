package com.zzx.gulimall.product.feign;

import com.zzx.common.to.es.SkuEsModel;
import com.zzx.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @author zzx
 * @date 2021-05-20 16:47
 */
@FeignClient("gulimall-search")
public interface SearchFeignService {
    /**
     * 调用远程接口向es发送数据
     *
     * @param skuEsModels
     * @return
     */
    @PostMapping("/search/save/product")
    R productStatusUp(@RequestBody List<SkuEsModel> skuEsModels);
}
