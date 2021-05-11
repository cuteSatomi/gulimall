package com.zzx.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zzx.common.utils.PageUtils;
import com.zzx.gulimall.product.entity.AttrGroupEntity;
import com.zzx.gulimall.product.vo.request.AttrGroupRelationVO;
import com.zzx.gulimall.product.vo.response.AttrGroupWithAttrsVO;

import java.util.List;
import java.util.Map;

/**
 * 属性分组
 *
 * @author zzx
 * @email hdsomedezzx@gmail.com
 * @date 2021-05-03 22:26:54
 */
public interface AttrGroupService extends IService<AttrGroupEntity> {

    PageUtils queryPage(Map<String, Object> params);

    PageUtils queryPage(Map<String, Object> params, Long catelogId);

    /**
     * 删除分组和属性的关联
     * @param attrRelationVOS
     * @return
     */
    void deleteAttrRelation(AttrGroupRelationVO[] attrRelationVOS);

    /**
     * 查询该分类下所有分组，以及分组下所有属性的集合
     * @param catelogId
     * @return
     */
    List<AttrGroupWithAttrsVO> getAttrGroupWithAttrs(Long catelogId);
}

