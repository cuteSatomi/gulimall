package com.zzx.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zzx.common.constant.ProductConstant;
import com.zzx.common.utils.PageUtils;
import com.zzx.common.utils.Query;
import com.zzx.gulimall.product.dao.AttrDao;
import com.zzx.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.zzx.gulimall.product.entity.AttrEntity;
import com.zzx.gulimall.product.entity.AttrGroupEntity;
import com.zzx.gulimall.product.entity.CategoryEntity;
import com.zzx.gulimall.product.service.AttrAttrgroupRelationService;
import com.zzx.gulimall.product.service.AttrGroupService;
import com.zzx.gulimall.product.service.AttrService;
import com.zzx.gulimall.product.service.CategoryService;
import com.zzx.gulimall.product.vo.AttrVO;
import com.zzx.gulimall.product.vo.response.AttrResponseVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("attrService")
public class AttrServiceImpl extends ServiceImpl<AttrDao, AttrEntity> implements AttrService {

    @Autowired
    private AttrAttrgroupRelationService relationService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private AttrGroupService attrGroupService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),
                new QueryWrapper<AttrEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 保存属性，同时维护中间表
     *
     * @param attrVO
     */
    @Override
    @Transactional
    public void saveAttr(AttrVO attrVO) {
        AttrEntity attrEntity = new AttrEntity();
        BeanUtils.copyProperties(attrVO, attrEntity);
        // 保存自身的表信息
        this.save(attrEntity);

        // 维护中间表信息，销售属性不需要维护中间表
        if (attrVO.getAttrType().equals(ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode())) {
            AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity();
            relationEntity.setAttrGroupId(attrVO.getAttrGroupId());
            relationEntity.setAttrId(attrEntity.getAttrId());
            relationService.save(relationEntity);
        }
    }

    /**
     * 分页查询属性列表
     *
     * @param params
     * @param catelogId
     * @param attrType
     * @return
     */
    @Override
    public PageUtils queryBaseAttrPage(Map<String, Object> params, Long catelogId, String attrType) {
        QueryWrapper<AttrEntity> queryWrapper = new QueryWrapper<AttrEntity>()
                .eq("attr_type", "base".equalsIgnoreCase(attrType) ?
                        ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode() :
                        ProductConstant.AttrEnum.ATTR_TYPE_SALE.getCode());

        if (catelogId != 0) {
            queryWrapper.eq("catelog_id", catelogId);
        }
        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            queryWrapper.and(wrapper -> wrapper.eq("attr_id", key).or().like("attr_name", key));
        }

        IPage<AttrEntity> page = this.page(new Query<AttrEntity>().getPage(params), queryWrapper);
        PageUtils pageUtils = new PageUtils(page);
        List<AttrEntity> records = page.getRecords();

        List<AttrResponseVO> result = records.stream().map(attrEntity -> {
            AttrResponseVO attrResponseVO = new AttrResponseVO();
            BeanUtils.copyProperties(attrEntity, attrResponseVO);

            if ("base".equalsIgnoreCase(attrType)) {
                AttrAttrgroupRelationEntity relationEntity = relationService.getOne(
                        new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrEntity.getAttrId()));
                if (relationEntity != null) {
                    AttrGroupEntity groupEntity = attrGroupService.getById(relationEntity.getAttrGroupId());
                    // 设置所属分组名字
                    attrResponseVO.setGroupName(groupEntity.getAttrGroupName());
                }
            }

            CategoryEntity categoryEntity = categoryService.getById(attrEntity.getCatelogId());
            if (categoryEntity != null) {
                // 设置所属分类名字
                attrResponseVO.setCatelogName(categoryEntity.getName());
            }

            // 返回前端所需的vo对象
            return attrResponseVO;
        }).collect(Collectors.toList());
        // 将处理后的list设置到分页对象中
        pageUtils.setList(result);
        return pageUtils;
    }

    /**
     * 根据属性id查询出完整的属性信息，包括所属分类以及所属分组
     *
     * @param attrId
     * @return
     */
    @Override
    public AttrResponseVO getAttrInfo(Long attrId) {
        AttrResponseVO responseVO = new AttrResponseVO();
        AttrEntity attrEntity = this.getById(attrId);
        // 将查询到do的属性复制给responseVO
        BeanUtils.copyProperties(attrEntity, responseVO);

        AttrGroupEntity groupEntity = new AttrGroupEntity();
        groupEntity.setCatelogId(attrEntity.getCatelogId());
        if (attrEntity.getAttrType().equals(ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode())) {
            // 给responseVO设置分组id
            AttrAttrgroupRelationEntity relationEntity = relationService.getOne(
                    new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrId));
            if (relationEntity != null) {
                responseVO.setAttrGroupId(relationEntity.getAttrGroupId());
                groupEntity = attrGroupService.getById(relationEntity.getAttrGroupId());
                if (groupEntity != null) {
                    responseVO.setGroupName(groupEntity.getAttrGroupName());
                }
            }
        }

        // 为responseVO设置完整分类路径
        Long catelogId = attrEntity.getCatelogId();
        categoryService.setCatelogPath(groupEntity);
        responseVO.setCatelogPath(groupEntity.getCatelogPath());

        CategoryEntity categoryEntity = categoryService.getById(catelogId);
        if (categoryEntity != null) {
            responseVO.setCatelogName(categoryEntity.getName());
        }
        return responseVO;
    }

    @Override
    @Transactional
    public void updateAttr(AttrVO attr) {
        AttrEntity attrEntity = new AttrEntity();
        BeanUtils.copyProperties(attr, attrEntity);
        this.updateById(attrEntity);

        if (attrEntity.getAttrType().equals(ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode())) {
            // 修改分组关联，是基本属性才来修改关联
            AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity();
            relationEntity.setAttrId(attr.getAttrId());
            relationEntity.setAttrGroupId(attr.getAttrGroupId());
            relationService.update(relationEntity, new UpdateWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attr.getAttrId()));
        }
    }

    @Override
    public List<AttrEntity> getAttrRelation(Long attrGroupId) {
        List<AttrAttrgroupRelationEntity> relationEntities = relationService.list(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_group_id", attrGroupId));
        List<Long> attrIds = relationEntities.stream().map(AttrAttrgroupRelationEntity::getAttrId).collect(Collectors.toList());
        if (attrIds.size() == 0) {
            return null;
        }
        List<AttrEntity> entityList = baseMapper.selectBatchIds(attrIds);
        return entityList;
    }

    /**
     * 分页查询当前分类未被其他组关联的参数
     *
     * @param attrGroupId
     * @param params
     * @return
     */
    @Override
    public PageUtils getAttrNoRelation(Long attrGroupId, Map<String, Object> params) {
        // 根据分组id查询出分类id
        AttrGroupEntity groupEntity = attrGroupService.getById(attrGroupId);
        Long catelogId = groupEntity.getCatelogId();
        // 根据分类id查询当前分类的分组集合
        List<AttrGroupEntity> groupList = attrGroupService.list(new QueryWrapper<AttrGroupEntity>().eq("catelog_id", catelogId));
        // 通过stream得到所有的分组id
        List<Long> groupIds = groupList.stream().map(AttrGroupEntity::getAttrGroupId).collect(Collectors.toList());
        // 从中间表查出已经被关联的attrId
        List<AttrAttrgroupRelationEntity> relationList = relationService.list(
                new QueryWrapper<AttrAttrgroupRelationEntity>().in("attr_group_id", groupIds));
        // 通过stream得到所有已经被关联的属性id
        List<Long> attrIds = relationList.stream().map(AttrAttrgroupRelationEntity::getAttrId).collect(Collectors.toList());
        // 能添加的属性必须是当前分类的属性，并且是基本属性
        QueryWrapper<AttrEntity> wrapper = new QueryWrapper<AttrEntity>().eq("catelog_id", catelogId)
                .eq("attr_type", ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode());
        if (attrIds.size() > 0) {
            // attrIds集合不为空时，去掉已经关联的属性
            wrapper.notIn("attr_id", attrIds);
        }
        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            wrapper.and(w -> w.eq("attr_id", key).or().like("attr_name", key));
        }
        IPage<AttrEntity> page = this.page(new Query<AttrEntity>().getPage(params), wrapper);

        PageUtils pageUtils = new PageUtils(page);

        return pageUtils;
    }

}