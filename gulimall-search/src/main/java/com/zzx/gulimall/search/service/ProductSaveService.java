package com.zzx.gulimall.search.service;

import com.zzx.common.to.es.SkuEsModel;

import java.io.IOException;
import java.util.List;

/**
 * @author zzx
 * @date 2021-05-20 15:59
 */
public interface ProductSaveService {
    /**
     * 将商品sku数据存入es中
     *
     * @param skuEsModels
     */
    void productStatusUp(List<SkuEsModel> skuEsModels) throws IOException;
}
