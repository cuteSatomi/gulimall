package com.zzx.gulimall.product.dao;

import com.zzx.gulimall.product.entity.CategoryEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品三级分类
 * 
 * @author zzx
 * @email hdsomedezzx@gmail.com
 * @date 2021-05-03 22:26:54
 */
@Mapper
public interface CategoryDao extends BaseMapper<CategoryEntity> {
	
}
