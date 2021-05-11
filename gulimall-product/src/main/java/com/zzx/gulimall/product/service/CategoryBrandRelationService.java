package com.zzx.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zzx.common.utils.PageUtils;
import com.zzx.gulimall.product.entity.BrandEntity;
import com.zzx.gulimall.product.entity.CategoryBrandRelationEntity;

import java.util.List;
import java.util.Map;

/**
 * 品牌分类关联
 *
 * @author zzx
 * @email hdsomedezzx@gmail.com
 * @date 2021-05-03 22:26:54
 */
public interface CategoryBrandRelationService extends IService<CategoryBrandRelationEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 保存品牌和分类的关联关系
     * @param categoryBrandRelation
     */
    void saveDetail(CategoryBrandRelationEntity categoryBrandRelation);

    /**
     * 更新中间表的品牌数据
     * @param brandId
     * @param name
     */
    void updateBrand(Long brandId, String name);

    /**
     * 更新中间表的分类数据
     * @param catId
     * @param name
     */
    void updateCategory(Long catId, String name);

    /**
     * 根据分类id查询出与分类相关联的品牌
     * @param catId
     * @return
     */
    List<BrandEntity> relationBrandsList(Long catId);
}

