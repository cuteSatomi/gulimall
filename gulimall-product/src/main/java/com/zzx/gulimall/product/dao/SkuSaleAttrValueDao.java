package com.zzx.gulimall.product.dao;

import com.zzx.gulimall.product.entity.SkuSaleAttrValueEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zzx.gulimall.product.vo.web.SkuItemVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * sku销售属性&值
 *
 * @author zzx
 * @email hdsomedezzx@gmail.com
 * @date 2021-05-03 22:26:54
 */
@Mapper
public interface SkuSaleAttrValueDao extends BaseMapper<SkuSaleAttrValueEntity> {

    /**
     * 根据spuId查询出所有的该spu下所有的sku的销售属性的组合
     *
     * @param spuId
     * @return
     */
    List<SkuItemVO.SkuItemSaleAttrVO> getSaleAttrsBySpuId(Long spuId);
}
