package com.zzx.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zzx.common.utils.PageUtils;
import com.zzx.gulimall.product.entity.SkuSaleAttrValueEntity;
import com.zzx.gulimall.product.vo.web.SkuItemVO;

import java.util.List;
import java.util.Map;

/**
 * sku销售属性&值
 *
 * @author zzx
 * @email hdsomedezzx@gmail.com
 * @date 2021-05-03 22:26:54
 */
public interface SkuSaleAttrValueService extends IService<SkuSaleAttrValueEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 根据spuId查询出所有的该spu下所有的sku的销售属性的组合
     *
     * @param spuId
     * @return
     */
    List<SkuItemVO.SkuItemSaleAttrVO> getSaleAttrsBySpuId(Long spuId);

    /**
     * 根据skuId获取该商品的销售属性，拼装成list集合
     *
     * @param skuId
     * @return
     */
    List<String> getSkuSaleAttrValueAsStringList(Long skuId);
}

