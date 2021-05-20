package com.zzx.gulimall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zzx.common.to.SkuHasStockTO;
import com.zzx.common.utils.PageUtils;
import com.zzx.gulimall.ware.entity.WareSkuEntity;

import java.util.List;
import java.util.Map;

/**
 * 商品库存
 *
 * @author zzx
 * @email hdsomedezzx@gmail.com
 * @date 2021-05-03 23:26:34
 */
public interface WareSkuService extends IService<WareSkuEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 查询sku是否有库存，供商品微服务远程调用
     *
     * @param skuIds
     * @return
     */
    List<SkuHasStockTO> getSkusHasStock(List<Long> skuIds);
}

