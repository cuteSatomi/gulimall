package com.zzx.gulimall.ware.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zzx.common.to.SkuHasStockTO;
import com.zzx.common.utils.PageUtils;
import com.zzx.common.utils.Query;
import com.zzx.gulimall.ware.dao.WareSkuDao;
import com.zzx.gulimall.ware.entity.WareSkuEntity;
import com.zzx.gulimall.ware.service.WareSkuService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<WareSkuEntity> wrapper = new QueryWrapper<>();
        Long skuId = (Long) params.get("skuId");
        if (!StringUtils.isEmpty(skuId)) {

        }
        Long wareId = (Long) params.get("wareId");

        IPage<WareSkuEntity> page = this.page(new Query<WareSkuEntity>().getPage(params), wrapper);

        return new PageUtils(page);
    }

    /**
     * 查询sku是否有库存，供商品微服务远程调用
     *
     * @param skuIds
     * @return
     */
    @Override
    public List<SkuHasStockTO> getSkusHasStock(List<Long> skuIds) {
        List<SkuHasStockTO> collect = skuIds.stream().map(skuId -> {
            SkuHasStockTO to = new SkuHasStockTO();
            long count = baseMapper.getSkuStock(skuId);
            to.setHasStock(count > 0);
            return to;
        }).collect(Collectors.toList());
        return collect;
    }

}