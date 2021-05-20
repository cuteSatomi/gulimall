package com.zzx.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zzx.common.utils.PageUtils;
import com.zzx.gulimall.product.entity.AttrEntity;
import com.zzx.gulimall.product.vo.AttrVO;
import com.zzx.gulimall.product.vo.response.AttrResponseVO;

import java.util.List;
import java.util.Map;

/**
 * 商品属性
 *
 * @author zzx
 * @email hdsomedezzx@gmail.com
 * @date 2021-05-03 22:26:54
 */
public interface AttrService extends IService<AttrEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 保存属性，同时维护中间表
     *
     * @param attrVO
     */
    void saveAttr(AttrVO attrVO);

    /**
     * 分页查询属性列表
     *
     * @param params
     * @param catelogId
     * @param attrType
     * @return
     */
    PageUtils queryBaseAttrPage(Map<String, Object> params, Long catelogId, String attrType);

    /**
     * 根据属性id查询出完整的属性信息，包括所属分类以及所属分组
     *
     * @param attrId
     * @return
     */
    AttrResponseVO getAttrInfo(Long attrId);

    /**
     * 更新属性
     *
     * @param attr
     */
    void updateAttr(AttrVO attr);

    /**
     * 根据分组id查询对应分组的全部属性
     *
     * @param attrGroupId
     * @return
     */
    List<AttrEntity> getAttrRelation(Long attrGroupId);

    /**
     * 分页查询当前分类未被其他组关联的参数
     *
     * @param attrGroupId
     * @param params
     * @return
     */
    PageUtils getAttrNoRelation(Long attrGroupId, Map<String, Object> params);

    /**
     * 在指定的所有属性集合中，挑选出检索属性
     * @param attrIds
     * @return
     */
    List<Long> selectSearchAttrsIds(List<Long> attrIds);
}

