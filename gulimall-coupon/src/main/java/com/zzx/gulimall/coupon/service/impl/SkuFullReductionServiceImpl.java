package com.zzx.gulimall.coupon.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zzx.common.to.MemberPrice;
import com.zzx.common.to.SkuReductionTO;
import com.zzx.common.utils.PageUtils;
import com.zzx.common.utils.Query;
import com.zzx.gulimall.coupon.dao.SkuFullReductionDao;
import com.zzx.gulimall.coupon.entity.MemberPriceEntity;
import com.zzx.gulimall.coupon.entity.SkuFullReductionEntity;
import com.zzx.gulimall.coupon.entity.SkuLadderEntity;
import com.zzx.gulimall.coupon.service.MemberPriceService;
import com.zzx.gulimall.coupon.service.SkuFullReductionService;
import com.zzx.gulimall.coupon.service.SkuLadderService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("skuFullReductionService")
public class SkuFullReductionServiceImpl extends ServiceImpl<SkuFullReductionDao, SkuFullReductionEntity> implements SkuFullReductionService {

    @Autowired
    private SkuLadderService skuLadderService;

    @Autowired
    private MemberPriceService memberPriceService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuFullReductionEntity> page = this.page(
                new Query<SkuFullReductionEntity>().getPage(params),
                new QueryWrapper<SkuFullReductionEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 保存sku的优惠，满减等信息
     *
     * @param reductionTO
     */
    @Override
    public void saveSkuReduction(SkuReductionTO reductionTO) {
        // sms_sku_ladder
        SkuLadderEntity skuLadderEntity = new SkuLadderEntity();
        skuLadderEntity.setSkuId(reductionTO.getSkuId());
        skuLadderEntity.setFullCount(reductionTO.getFullCount());
        skuLadderEntity.setDiscount(reductionTO.getDiscount());
        skuLadderEntity.setAddOther(reductionTO.getCountStatus());
        skuLadderService.save(skuLadderEntity);

        // sms_sku_full_reduction
        SkuFullReductionEntity fullReductionEntity = new SkuFullReductionEntity();
        BeanUtils.copyProperties(reductionTO, fullReductionEntity);
        fullReductionEntity.setAddOther(reductionTO.getCountStatus());
        this.save(fullReductionEntity);

        // sms_member_price
        List<MemberPrice> memberPrice = reductionTO.getMemberPrice();
        List<MemberPriceEntity> collect = memberPrice.stream().map(item -> {
            MemberPriceEntity priceEntity = new MemberPriceEntity();
            priceEntity.setSkuId(reductionTO.getSkuId());
            priceEntity.setMemberLevelId(item.getId());
            priceEntity.setMemberLevelName(item.getName());
            priceEntity.setMemberPrice(item.getPrice());
            // 默认叠加其他优惠
            priceEntity.setAddOther(1);
            return priceEntity;
        }).collect(Collectors.toList());
        memberPriceService.saveBatch(collect);
    }

}