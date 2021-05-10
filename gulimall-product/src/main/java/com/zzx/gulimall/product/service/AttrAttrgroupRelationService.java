package com.zzx.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zzx.common.utils.PageUtils;
import com.zzx.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.zzx.gulimall.product.vo.request.AttrGroupRelationVO;

import java.util.List;
import java.util.Map;

/**
 * 属性&属性分组关联
 *
 * @author zzx
 * @email hdsomedezzx@gmail.com
 * @date 2021-05-03 22:26:54
 */
public interface AttrAttrgroupRelationService extends IService<AttrAttrgroupRelationEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 批量删除分组与属性的关联关系
     *
     * @param entities
     */
    void deleteBatchRelation(List<AttrAttrgroupRelationEntity> entities);

    /**
     * 添加关联关系
     * @param vos
     */
    void addRelation(List<AttrGroupRelationVO> vos);
}

