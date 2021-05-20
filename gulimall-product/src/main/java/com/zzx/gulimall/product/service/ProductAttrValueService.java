package com.zzx.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zzx.common.utils.PageUtils;
import com.zzx.gulimall.product.entity.ProductAttrValueEntity;

import java.util.List;
import java.util.Map;

/**
 * spu属性值
 *
 * @author zzx
 * @email hdsomedezzx@gmail.com
 * @date 2021-05-03 22:26:54
 */
public interface ProductAttrValueService extends IService<ProductAttrValueEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 保存spu的规格参数
     *
     * @param collect
     */
    void saveProductAttr(List<ProductAttrValueEntity> collect);

    /**
     * 根据spuId查询规格参数
     * @param spuId
     * @return
     */
    public List<ProductAttrValueEntity> baseAttrListForSpu(Long spuId);
}

