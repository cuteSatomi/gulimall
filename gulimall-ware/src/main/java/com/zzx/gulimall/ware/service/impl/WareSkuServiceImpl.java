package com.zzx.gulimall.ware.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zzx.common.to.SkuHasStockTO;
import com.zzx.common.utils.PageUtils;
import com.zzx.common.utils.Query;
import com.zzx.gulimall.ware.dao.WareSkuDao;
import com.zzx.gulimall.ware.entity.WareSkuEntity;
import com.zzx.gulimall.ware.exception.NoStockException;
import com.zzx.gulimall.ware.service.WareSkuService;
import com.zzx.gulimall.ware.vo.OrderItemVO;
import com.zzx.gulimall.ware.vo.WareSkuLockVO;
import lombok.Data;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
            Long count = baseMapper.getSkuStock(skuId);
            to.setSkuId(skuId);
            to.setHasStock(count == null ? false : count > 0);
            return to;
        }).collect(Collectors.toList());
        return collect;
    }

    /**
     * 为某个订单锁定库存
     *
     * @param vo
     * @return
     */
    @Transactional
    @Override
    public Boolean orderLockStock(WareSkuLockVO vo) {

        List<OrderItemVO> locks = vo.getLocks();

        // 找到这个sku在哪些仓库有库存
        List<SkuWareHasStock> collect = locks.stream().map(item -> {
            SkuWareHasStock skuWareHasStock = new SkuWareHasStock();
            Long skuId = item.getSkuId();
            skuWareHasStock.setSkuId(skuId);
            skuWareHasStock.setNum(item.getCount());
            // 查询这个商品在哪些仓库有库存
            List<Long> wareIds = baseMapper.listWareIdsHasStock(skuId);
            skuWareHasStock.setWareIds(wareIds);
            return skuWareHasStock;
        }).collect(Collectors.toList());

        // 锁定库存
        for (SkuWareHasStock hasStock : collect) {
            boolean skuStock = false;
            Long skuId = hasStock.getSkuId();
            List<Long> wareIds = hasStock.getWareIds();
            if (wareIds == null || wareIds.size() == 0) {
                throw new NoStockException(skuId);
            }

            for (Long wareId : wareIds) {
                // 成功返回1，失败返回0
                Long count = baseMapper.lockSkuStock(skuId, wareId, hasStock.getNum());
                if (count == 1) {
                    // 锁定成功，将标志位置为true，退出循环无须判断其他仓库
                    skuStock = true;
                    break;
                } else {
                    // 当前仓库锁定失败，尝试下一个仓库

                }
            }
            if (!skuStock) {
                throw new NoStockException(skuId);
            }

        }

        return true;
    }

    @Data
    static class SkuWareHasStock {
        private Long skuId;
        private Integer num;
        private List<Long> wareIds;
    }

}