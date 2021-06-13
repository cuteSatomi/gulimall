package com.zzx.gulimall.coupon.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zzx.common.to.SkuReductionTo;
import com.zzx.common.utils.PageUtils;
import com.zzx.gulimall.coupon.entity.SkuFullReductionEntity;

import java.util.Map;

/**
 * 商品满减信息
 *
 * @author zzx
 * @email hdsomedezzx@gmail.com
 * @date 2021-05-03 23:00:50
 */
public interface SkuFullReductionService extends IService<SkuFullReductionEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 保存sku的优惠，满减等信息
     *
     * @param skuReductionTO
     */
    void saveSkuReduction(SkuReductionTo skuReductionTO);
}

