package com.zzx.gulimall.ware.dao;

import com.zzx.gulimall.ware.entity.WareSkuEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商品库存
 *
 * @author zzx
 * @email hdsomedezzx@gmail.com
 * @date 2021-05-03 23:26:34
 */
@Mapper
public interface WareSkuDao extends BaseMapper<WareSkuEntity> {

    /**
     * 根据skuId查询是否有库存
     *
     * @param skuId
     * @return
     */
    Long getSkuStock(Long skuId);

    /**
     * 查询这个商品在哪些仓库有库存
     *
     * @param skuId
     * @return
     */
    List<Long> listWareIdsHasStock(Long skuId);

    /**
     * 锁定库存
     *
     * @param skuId
     * @param wareId
     * @param num
     * @return
     */
    Long lockSkuStock(@Param("skuId") Long skuId, @Param("wareId") Long wareId, @Param("num") Integer num);

    /**
     * 解锁库存
     *
     * @param skuId  sku商品的id
     * @param wareId 仓库id
     * @param skuNum 解锁商品的数量
     */
    void unlockStock(@Param("skuId") Long skuId, @Param("wareId") Long wareId, @Param("skuNum") Integer skuNum);
}
