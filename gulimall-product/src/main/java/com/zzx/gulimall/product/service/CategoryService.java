package com.zzx.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zzx.common.utils.PageUtils;
import com.zzx.gulimall.product.entity.AttrGroupEntity;
import com.zzx.gulimall.product.entity.CategoryEntity;
import com.zzx.gulimall.product.vo.web.Catelog2VO;

import java.util.List;
import java.util.Map;

/**
 * 商品三级分类
 *
 * @author zzx
 * @email hdsomedezzx@gmail.com
 * @date 2021-05-03 22:26:54
 */
public interface CategoryService extends IService<CategoryEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 查询出所有的分类及其子分类，按照树形结构组织起来
     *
     * @return
     */
    List<CategoryEntity> listWithTree();

    /**
     * 为attrGroup设置完整的分组路径
     *
     * @param attrGroup
     */
    void setCatelogPath(AttrGroupEntity attrGroup);

    /**
     * 更新category的细节，包括其他表中关于category表的冗余字段
     *
     * @param category
     */
    void updateDetail(CategoryEntity category);

    /**
     * 获取所有一级分类
     *
     * @return
     */
    List<CategoryEntity> getLevel1Categories();

    /**
     * 查出所有分类
     *
     * @return
     */
    Map<String, List<Catelog2VO>> getCatalogJson();
}

