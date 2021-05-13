package com.zzx.gulimall.ware.controller;

import com.zzx.common.utils.PageUtils;
import com.zzx.common.utils.R;
import com.zzx.gulimall.ware.entity.PurchaseEntity;
import com.zzx.gulimall.ware.service.PurchaseService;
import com.zzx.gulimall.ware.vo.request.MergePurchaseVO;
import com.zzx.gulimall.ware.vo.request.PurchaseDoneVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;


/**
 * 采购信息
 *
 * @author zzx
 * @email hdsomedezzx@gmail.com
 * @date 2021-05-03 23:26:34
 */
@RestController
@RequestMapping("ware/purchase")
public class PurchaseController {
    @Autowired
    private PurchaseService purchaseService;

    /**
     * 完成采购
     *
     * @param doneVO
     * @return
     */
    @PostMapping("/done")
    public R done(@RequestBody PurchaseDoneVO doneVO) {
        purchaseService.done(doneVO);

        return R.ok();
    }

    /**
     * 领取采购单
     *
     * @param purchaseIds
     * @return
     */
    @PostMapping("/received")
    public R received(@RequestBody List<Long> purchaseIds) {
        purchaseService.received(purchaseIds);

        return R.ok();
    }

    /**
     * 合并多个采购需求到一个采购单
     *
     * @param mergePurchaseVO
     * @return
     */
    @PostMapping("/merge")
    public R mergePurchase(@RequestBody MergePurchaseVO mergePurchaseVO) {
        purchaseService.mergePurchase(mergePurchaseVO);

        return R.ok();
    }

    /**
     * 查询出未被领取的采购单
     *
     * @param params
     * @return
     */
    @GetMapping("/unreceive/list")
    // @RequiresPermissions("ware:purchase:list")
    public R unReceiveList(@RequestParam Map<String, Object> params) {
        PageUtils page = purchaseService.queryPageUnReceive(params);

        return R.ok().put("page", page);
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    // @RequiresPermissions("ware:purchase:list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = purchaseService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    // @RequiresPermissions("ware:purchase:info")
    public R info(@PathVariable("id") Long id) {
        PurchaseEntity purchase = purchaseService.getById(id);

        return R.ok().put("purchase", purchase);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    // @RequiresPermissions("ware:purchase:save")
    public R save(@RequestBody PurchaseEntity purchase) {
        purchase.setCreateTime(new Date());
        purchase.setUpdateTime(new Date());
        purchaseService.save(purchase);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    // @RequiresPermissions("ware:purchase:update")
    public R update(@RequestBody PurchaseEntity purchase) {
        purchaseService.updateById(purchase);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    // @RequiresPermissions("ware:purchase:delete")
    public R delete(@RequestBody Long[] ids) {
        purchaseService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
