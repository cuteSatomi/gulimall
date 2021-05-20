package com.zzx.gulimall.search.controller;

import com.zzx.common.gulienum.BizCodeEnum;
import com.zzx.common.to.es.SkuEsModel;
import com.zzx.common.utils.R;
import com.zzx.gulimall.search.service.ProductSaveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

/**
 * @author zzx
 * @date 2021-05-20 15:56
 */
@RestController
@RequestMapping("/search/save")
public class ElasticSaveController {

    @Autowired
    private ProductSaveService productSaveService;

    /**
     * 将商品sku数据存入es中
     *
     * @param skuEsModels
     * @return
     */
    @PostMapping("/product")
    public R productStatusUp(@RequestBody List<SkuEsModel> skuEsModels) {
        try {
            productSaveService.productStatusUp(skuEsModels);
        } catch (IOException e) {
            e.printStackTrace();
            return R.error(BizCodeEnum.PRODUCT_UP_EXCEPTION.getCode(), BizCodeEnum.PRODUCT_UP_EXCEPTION.getMsg());
        }
        return R.ok();
    }
}
