package com.zzx.gulimall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zzx.common.utils.PageUtils;
import com.zzx.gulimall.ware.entity.PurchaseEntity;
import com.zzx.gulimall.ware.vo.request.MergePurchaseVO;
import com.zzx.gulimall.ware.vo.request.PurchaseDoneVO;

import java.util.List;
import java.util.Map;

/**
 * 采购信息
 *
 * @author zzx
 * @email hdsomedezzx@gmail.com
 * @date 2021-05-03 23:26:34
 */
public interface PurchaseService extends IService<PurchaseEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 查询出未被领取的采购单
     *
     * @param params
     * @return
     */
    PageUtils queryPageUnReceive(Map<String, Object> params);

    /**
     * 合并多个采购需求到一个采购单
     *
     * @param mergePurchaseVO
     */
    void mergePurchase(MergePurchaseVO mergePurchaseVO);

    /**
     * 领取采购单
     *
     * @param purchaseIds
     */
    void received(List<Long> purchaseIds);

    /**
     * 完成采购
     * @param doneVO
     */
    void done(PurchaseDoneVO doneVO);
}

