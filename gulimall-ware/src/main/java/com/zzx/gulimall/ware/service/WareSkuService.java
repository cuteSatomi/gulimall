package com.zzx.gulimall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zzx.common.to.SkuHasStockTo;
import com.zzx.common.to.mq.OrderTo;
import com.zzx.common.to.mq.StockLockedTo;
import com.zzx.common.utils.PageUtils;
import com.zzx.gulimall.ware.entity.WareSkuEntity;
import com.zzx.gulimall.ware.vo.WareSkuLockVO;

import java.util.List;
import java.util.Map;

/**
 * 商品库存
 *
 * @author zzx
 * @email hdsomedezzx@gmail.com
 * @date 2021-05-03 23:26:34
 */
public interface WareSkuService extends IService<WareSkuEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 查询sku是否有库存，供商品微服务远程调用
     *
     * @param skuIds
     * @return
     */
    List<SkuHasStockTo> getSkusHasStock(List<Long> skuIds);

    /**
     * 为某个订单锁定库存
     *
     * @param vo
     * @return
     */
    Boolean orderLockStock(WareSkuLockVO vo);

    void unlockStock(StockLockedTo to);

    /**
     * 订单关闭主动发消息准备解锁库存
     *
     * @param to
     */
    void unlockStock(OrderTo to);
}

