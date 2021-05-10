package com.zzx.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zzx.common.utils.PageUtils;
import com.zzx.common.utils.Query;
import com.zzx.gulimall.product.dao.CategoryDao;
import com.zzx.gulimall.product.entity.AttrGroupEntity;
import com.zzx.gulimall.product.entity.CategoryEntity;
import com.zzx.gulimall.product.service.CategoryBrandRelationService;
import com.zzx.gulimall.product.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Autowired
    private CategoryBrandRelationService categoryBrandRelationService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 查询出所有的分类及其子分类，按照树形结构组织起来
     *
     * @return
     */
    @Override
    public List<CategoryEntity> listWithTree() {
        // 查询出所有分类
        List<CategoryEntity> categoryList = baseMapper.selectList(null);
        List<CategoryEntity> level1Menus = categoryList.stream()
                .filter(category -> category.getParentCid() == 0)
                .peek(menu -> menu.setChildren(getChildren(menu, categoryList)))
                .sorted(Comparator.comparingInt(menu -> (menu.getSort() == null ? 0 : menu.getSort())))
                .collect(Collectors.toList());

        return level1Menus;
    }

    @Override
    public void setCatelogPath(AttrGroupEntity attrGroup) {
        List<Long> paths = new ArrayList<>();
        findParentPath(attrGroup.getCatelogId(), paths);
        Collections.reverse(paths);
        Long[] catelogPath = paths.toArray(new Long[paths.size()]);
        attrGroup.setCatelogPath(catelogPath);
    }

    @Override
    public void updateDetail(CategoryEntity category) {
        // 首先更新category表自身
        this.updateById(category);
        // 如果此次更新涉及brand的name字段
        if(!StringUtils.isEmpty(category.getName())){
            // 更新中间表的brandName
            categoryBrandRelationService.updateCategory(category.getCatId(),category.getName());
        }
    }

    /**
     * 递归查询分类的完整路径
     *
     * @param catelogId
     * @param paths
     */
    public void findParentPath(Long catelogId, List<Long> paths) {
        CategoryEntity entity = this.getById(catelogId);
        paths.add(catelogId);
        if (entity.getParentCid() != 0) {
            findParentPath(entity.getParentCid(), paths);
        }
    }

    /**
     * 递归查询所有菜单的子菜单
     *
     * @param root
     * @param all
     * @return
     */
    public List<CategoryEntity> getChildren(CategoryEntity root, List<CategoryEntity> all) {
        List<CategoryEntity> children = all.stream()
                .filter(category -> category.getParentCid().equals(root.getCatId()))
                .peek(category -> category.setChildren(getChildren(category, all)))
                .sorted(Comparator.comparingInt(menu -> (menu.getSort() == null ? 0 : menu.getSort())))
                .collect(Collectors.toList());
        return children;
    }
}