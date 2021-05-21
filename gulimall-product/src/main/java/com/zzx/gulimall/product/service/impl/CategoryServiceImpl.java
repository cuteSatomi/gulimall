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
import com.zzx.gulimall.product.vo.web.Catelog2VO;
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
        if (!StringUtils.isEmpty(category.getName())) {
            // 更新中间表的brandName
            categoryBrandRelationService.updateCategory(category.getCatId(), category.getName());
        }
    }

    /**
     * 获取所有一级分类
     *
     * @return
     */
    @Override
    public List<CategoryEntity> getLevel1Categories() {
        return baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", 0));
    }

    /**
     * 查出所有分类
     *
     * @return
     */
    @Override
    public Map<String, List<Catelog2VO>> getCatalogJson() {
        // 查出所有1级分类
        List<CategoryEntity> level1Categories = getLevel1Categories();
        Map<String, List<Catelog2VO>> map = level1Categories.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
            // 查询当前一级分类下的所有二级分类
            List<CategoryEntity> l2CategoryList = baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", v.getCatId()));
            List<Catelog2VO> catelog2VOS = null;
            if (l2CategoryList != null && l2CategoryList.size() > 0) {
                catelog2VOS = l2CategoryList.stream().map(l2 -> {
                    Catelog2VO catelog2VO = new Catelog2VO(v.getCatId().toString(), null, l2.getCatId().toString(), l2.getName());
                    // 再根据当前二级分类id查询所有三级分类
                    List<CategoryEntity> l3CategoryList = baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", l2.getCatId()));
                    if (l3CategoryList != null && l3CategoryList.size() > 0) {
                        List<Catelog2VO.Catelog3VO> collect = l3CategoryList.stream().map(l3 -> {
                            Catelog2VO.Catelog3VO catelog3VO = new Catelog2VO.Catelog3VO(l2.getCatId().toString(), l3.getCatId().toString(), l3.getName());
                            return catelog3VO;
                        }).collect(Collectors.toList());
                        catelog2VO.setCatalog3List(collect);
                    }
                    return catelog2VO;
                }).collect(Collectors.toList());
            }
            return catelog2VOS;
        }));
        return map;
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