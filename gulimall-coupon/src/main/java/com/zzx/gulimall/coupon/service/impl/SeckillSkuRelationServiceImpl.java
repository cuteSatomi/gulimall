package com.zzx.gulimall.coupon.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zzx.common.utils.PageUtils;
import com.zzx.common.utils.Query;
import com.zzx.gulimall.coupon.dao.SeckillSkuRelationDao;
import com.zzx.gulimall.coupon.entity.SeckillSkuRelationEntity;
import com.zzx.gulimall.coupon.service.SeckillSkuRelationService;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Map;


@Service("seckillSkuRelationService")
public class SeckillSkuRelationServiceImpl extends ServiceImpl<SeckillSkuRelationDao, SeckillSkuRelationEntity> implements SeckillSkuRelationService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<SeckillSkuRelationEntity> queryWrapper = new QueryWrapper<>();
        String promotionSessionId = (String) params.get("promotionSessionId");
        if (StringUtils.isNotBlank(promotionSessionId)) {
            queryWrapper.eq("promotion_session_id",promotionSessionId);
        }
        IPage<SeckillSkuRelationEntity> page = this.page(
                new Query<SeckillSkuRelationEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

}