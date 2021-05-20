package com.zzx.gulimall.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.zzx.common.to.es.SkuEsModel;
import com.zzx.gulimall.search.config.GulimallElasticSearchConfig;
import com.zzx.gulimall.search.constant.EsConstant;
import com.zzx.gulimall.search.service.ProductSaveService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

/**
 * @author zzx
 * @date 2021-05-20 16:02
 */
@Slf4j
@Service
public class ProductSaveServiceImpl implements ProductSaveService {

    @Autowired
    private RestHighLevelClient client;

    /**
     * 将商品sku数据存入es中
     *
     * @param skuEsModels
     */
    @Override
    public void productStatusUp(List<SkuEsModel> skuEsModels) throws IOException {
        BulkRequest bulkRequest = new BulkRequest();
        for (SkuEsModel model : skuEsModels) {
            // 构造保存请求
            IndexRequest indexRequest = new IndexRequest(EsConstant.PRODUCT_INDEX);
            indexRequest.id(model.getSkuId().toString());
            String jsonString = JSON.toJSONString(model);
            indexRequest.source(jsonString, XContentType.JSON);
            bulkRequest.add(indexRequest);
        }
        // 为es保存数据
        BulkResponse responses = client.bulk(bulkRequest, GulimallElasticSearchConfig.COMMON_OPTIONS);

        // TODO 如果批量出现错误
        boolean b = responses.hasFailures();
        log.error("商品上架错误");

    }
}
