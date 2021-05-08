package com.zzx.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zzx.common.utils.PageUtils;
import com.zzx.common.utils.Query;
import com.zzx.gulimall.product.dao.BrandDao;
import com.zzx.gulimall.product.entity.BrandEntity;
import com.zzx.gulimall.product.service.BrandService;
import com.zzx.gulimall.product.service.CategoryBrandRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Map;


@Service("brandService")
public class BrandServiceImpl extends ServiceImpl<BrandDao, BrandEntity> implements BrandService {

    @Autowired
    private CategoryBrandRelationService categoryBrandRelationService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        String key = (String) params.get("key");
        QueryWrapper<BrandEntity> wrapper = new QueryWrapper<>();
        if (!StringUtils.isEmpty(key)) {
            wrapper.eq("brand_id", key).or().like("name", key);
        }

        IPage<BrandEntity> page = this.page(new Query<BrandEntity>().getPage(params), wrapper);

        return new PageUtils(page);
    }

    @Override
    public void updateDetail(BrandEntity brand) {
        // 首先更新brand表自身
        this.updateById(brand);
        // 如果此次更新涉及brand的name字段
        if(!StringUtils.isEmpty(brand.getName())){
            // 更新中间表的brandName
            categoryBrandRelationService.updateBrand(brand.getBrandId(),brand.getName());
        }
    }

}