package com.zzx.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zzx.common.utils.PageUtils;
import com.zzx.common.utils.Query;
import com.zzx.gulimall.product.dao.SkuInfoDao;
import com.zzx.gulimall.product.entity.SkuImagesEntity;
import com.zzx.gulimall.product.entity.SkuInfoEntity;
import com.zzx.gulimall.product.entity.SpuInfoDescEntity;
import com.zzx.gulimall.product.service.*;
import com.zzx.gulimall.product.vo.web.SkuItemVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;


@Service("skuInfoService")
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoDao, SkuInfoEntity> implements SkuInfoService {

    @Autowired
    private SkuImagesService skuImagesService;

    @Autowired
    private SkuSaleAttrValueService skuSaleAttrValueService;

    @Autowired
    private SpuInfoDescService spuInfoDescService;

    @Autowired
    private AttrGroupService attrGroupService;

    @Autowired
    private ThreadPoolExecutor executor;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                new QueryWrapper<SkuInfoEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 保存sku的基本信息
     *
     * @param skuInfoEntity
     */
    @Override
    public void saveSkuInfo(SkuInfoEntity skuInfoEntity) {
        baseMapper.insert(skuInfoEntity);
    }

    /**
     * 根据skuId查询出详情页面所需的数据
     *
     * @param skuId
     * @return
     */
    @Override
    public SkuItemVO item(Long skuId) throws ExecutionException, InterruptedException {
        SkuItemVO skuItemVO = new SkuItemVO();

        CompletableFuture<SkuInfoEntity> infoFuture = CompletableFuture.supplyAsync(() -> {
            // 1、sku基本信息获取，相关表：pms_sku_info
            SkuInfoEntity skuInfo = getById(skuId);
            skuItemVO.setSkuInfo(skuInfo);
            return skuInfo;
        }, executor);

        CompletableFuture<Void> imagesFuture = CompletableFuture.runAsync(() -> {
            // 2、sku的图片信息，相关表：pms_sku_images
            List<SkuImagesEntity> images = skuImagesService.getImagesBuSkuId(skuId);
            skuItemVO.setImages(images);
        }, executor);

        CompletableFuture<Void> saleAttrFuture = infoFuture.thenAcceptAsync(result -> {
            // 3、spu的销售属性组合
            List<SkuItemVO.SkuItemSaleAttrVO> saleAttrVO = skuSaleAttrValueService.getSaleAttrsBySpuId(result.getSpuId());
            skuItemVO.setSaleAttr(saleAttrVO);
        }, executor);

        CompletableFuture<Void> infoDescFuture = infoFuture.thenAcceptAsync(result -> {
            // 4、spu介绍，相关表：pms_spu_info_desc
            SpuInfoDescEntity spuInfoDesc = spuInfoDescService.getById(result.getSpuId());
            skuItemVO.setDesc(spuInfoDesc);
        }, executor);

        CompletableFuture<Void> attrGroupFuture = infoFuture.thenAcceptAsync(result -> {
            // 5、spu的规格参数信息
            List<SkuItemVO.SpuItemAttrGroupVO> attrGroupVos = attrGroupService
                    .getAttrGroupWithAttrsBySpuId(result.getSpuId(), result.getCatalogId());
            skuItemVO.setGroupAttrs(attrGroupVos);
        }, executor);

        // 等待所有任务完成
        CompletableFuture.allOf(imagesFuture,saleAttrFuture,infoDescFuture,attrGroupFuture).get();

        return skuItemVO;
    }

}