package com.zzx.gulimall.ware.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zzx.common.constant.WareConstant;
import com.zzx.common.utils.PageUtils;
import com.zzx.common.utils.Query;
import com.zzx.gulimall.ware.dao.PurchaseDao;
import com.zzx.gulimall.ware.entity.PurchaseDetailEntity;
import com.zzx.gulimall.ware.entity.PurchaseEntity;
import com.zzx.gulimall.ware.service.PurchaseDetailService;
import com.zzx.gulimall.ware.service.PurchaseService;
import com.zzx.gulimall.ware.vo.request.MergePurchaseVO;
import com.zzx.gulimall.ware.vo.request.PurchaseDetailDoneVO;
import com.zzx.gulimall.ware.vo.request.PurchaseDoneVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;


@Service("purchaseService")
public class PurchaseServiceImpl extends ServiceImpl<PurchaseDao, PurchaseEntity> implements PurchaseService {

    @Autowired
    private PurchaseDetailService detailService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                new QueryWrapper<PurchaseEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 查询出未被领取的采购单
     *
     * @param params
     * @return
     */
    @Override
    public PageUtils queryPageUnReceive(Map<String, Object> params) {


        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                new QueryWrapper<PurchaseEntity>().eq("status", 0).or().eq("status", 1)
        );

        return new PageUtils(page);
    }

    /**
     * 合并多个采购需求到一个采购单
     *
     * @param mergePurchaseVO
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public void mergePurchase(MergePurchaseVO mergePurchaseVO) {
        Long purchaseId = mergePurchaseVO.getPurchaseId();

        if (purchaseId == null) {
            // 没有选择采购单则新建一个采购单合并
            PurchaseEntity purchaseEntity = new PurchaseEntity();
            purchaseEntity.setStatus(WareConstant.PurchaseStatusEnum.CREATED.getCode());
            purchaseEntity.setCreateTime(new Date());
            purchaseEntity.setUpdateTime(new Date());
            this.save(purchaseEntity);
            // 保存完成，获取采购单id
            purchaseId = purchaseEntity.getId();
        }

        // 合并操作，其实就是将多个采购需求和同一个采购单关联
        List<Long> items = mergePurchaseVO.getItems();
        Long finalPurchaseId = purchaseId;
        List<PurchaseDetailEntity> collect = items.stream().map(item -> {
            PurchaseDetailEntity detailEntity = new PurchaseDetailEntity();
            detailEntity.setId(item);
            detailEntity.setPurchaseId(finalPurchaseId);
            detailEntity.setStatus(WareConstant.PurchaseDetailStatusEnum.ASSIGNED.getCode());
            return detailEntity;
        }).collect(Collectors.toList());
        // 批量更新
        detailService.updateBatchById(collect);
    }

    /**
     * 领取采购单
     *
     * @param purchaseIds
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public void received(List<Long> purchaseIds) {
        // 根据id集合批量查询采购单
        Collection<PurchaseEntity> purchaseEntities = listByIds(purchaseIds);
        List<PurchaseEntity> purchaseList = purchaseEntities.stream()
                // 过滤掉状态不是新建或者已分配的采购单
                .filter(purchase -> purchase.getStatus().equals(WareConstant.PurchaseStatusEnum.CREATED.getCode()) ||
                        purchase.getStatus().equals(WareConstant.PurchaseStatusEnum.ASSIGNED.getCode()))
                // 将符合条件的采购单状态设置为已领取
                .peek(purchase -> {
                    purchase.setStatus(WareConstant.PurchaseStatusEnum.RECEIVE.getCode());
                    purchase.setUpdateTime(new Date());
                })
                .collect(Collectors.toList());
        // 批量更新采购单
        updateBatchById(purchaseList);

        // 改变采购项的状态
        // 根据采购单id查询采购项目集合
        List<PurchaseDetailEntity> detailEntityList = detailService.list(
                new QueryWrapper<PurchaseDetailEntity>().in("purchase_id", purchaseIds));
        // 将采购项目的状态设置为采购中
        List<PurchaseDetailEntity> collect = detailEntityList.stream()
                .map(detailEntity -> {
                    PurchaseDetailEntity detailEntity1 = new PurchaseDetailEntity();
                    detailEntity1.setId(detailEntity.getId());
                    detailEntity1.setStatus(WareConstant.PurchaseDetailStatusEnum.BUYING.getCode());
                    return detailEntity1;
                }).collect(Collectors.toList());

        detailService.updateBatchById(collect);
    }

    /**
     * 完成采购
     *
     * @param doneVO
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public void done(PurchaseDoneVO doneVO) {
        // 改变采购项状态
        Boolean flag = true;
        List<PurchaseDetailDoneVO> items = doneVO.getItems();
        List<PurchaseDetailEntity> updates = new ArrayList<PurchaseDetailEntity>();
        for (PurchaseDetailDoneVO item : items) {
            PurchaseDetailEntity detailEntity = new PurchaseDetailEntity();
            // 如果有采购失败，则设置为false
            if (WareConstant.PurchaseDetailStatusEnum.FAILED.getCode().equals(item.getStatus())) {
                flag = false;
                detailEntity.setStatus(item.getStatus());
            } else {
                detailEntity.setStatus(WareConstant.PurchaseDetailStatusEnum.FINISHED.getCode());
            }
            detailEntity.setId(item.getItemId());
            updates.add(detailEntity);
        }
        // 批量更新
        detailService.updateBatchById(updates);

        // 根据采购项的完成状态来设置采购单的状态
        Long id = doneVO.getId();
        PurchaseEntity purchaseEntity = new PurchaseEntity();
        purchaseEntity.setId(id);
        purchaseEntity.setStatus(flag ? WareConstant.PurchaseStatusEnum.FINISHED.getCode() : WareConstant.PurchaseStatusEnum.HAS_ERROR.getCode());
        purchaseEntity.setUpdateTime(new Date());

        // 将成功的采购入库
    }

}