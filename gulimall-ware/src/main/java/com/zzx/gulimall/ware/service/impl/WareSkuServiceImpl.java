package com.zzx.gulimall.ware.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zzx.common.to.SkuHasStockTo;
import com.zzx.common.to.mq.OrderTo;
import com.zzx.common.to.mq.StockDetailTo;
import com.zzx.common.to.mq.StockLockedTo;
import com.zzx.common.utils.PageUtils;
import com.zzx.common.utils.Query;
import com.zzx.common.utils.R;
import com.zzx.gulimall.ware.dao.WareSkuDao;
import com.zzx.gulimall.ware.entity.WareOrderTaskDetailEntity;
import com.zzx.gulimall.ware.entity.WareOrderTaskEntity;
import com.zzx.gulimall.ware.entity.WareSkuEntity;
import com.zzx.gulimall.ware.exception.NoStockException;
import com.zzx.gulimall.ware.feign.OrderFeignService;
import com.zzx.gulimall.ware.service.WareOrderTaskDetailService;
import com.zzx.gulimall.ware.service.WareOrderTaskService;
import com.zzx.gulimall.ware.service.WareSkuService;
import com.zzx.gulimall.ware.vo.OrderItemVO;
import com.zzx.gulimall.ware.vo.OrderVO;
import com.zzx.gulimall.ware.vo.WareSkuLockVO;
import lombok.Data;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private WareOrderTaskService orderTaskService;

    @Autowired
    private WareOrderTaskDetailService orderTaskDetailService;

    @Autowired
    private OrderFeignService orderFeignService;

    /**
     * 解锁库存
     *
     * @param skuId  sku商品的id
     * @param wareId 仓库id
     * @param skuNum 解锁商品的数量
     */
    private void unlockStock(Long skuId, Long wareId, Integer skuNum) {
        baseMapper.unlockStock(skuId, wareId, skuNum);
        // TODO 解锁完库存以后，需要将对应的工作单的status改为2，表示已解锁
    }

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
    public List<SkuHasStockTo> getSkusHasStock(List<Long> skuIds) {
        List<SkuHasStockTo> collect = skuIds.stream().map(skuId -> {
            SkuHasStockTo to = new SkuHasStockTo();
            Long count = baseMapper.getSkuStock(skuId);
            to.setSkuId(skuId);
            to.setHasStock(count == null ? false : count > 0);
            return to;
        }).collect(Collectors.toList());
        return collect;
    }

    /**
     * 为某个订单锁定库存
     * <p>
     * 库存解锁场景
     * 1、下单成功但是支付超时
     * 2、用户手动取消订单
     * 3、下单和库存锁定成功，但是其他业务的调用失败导致订单回滚
     *
     * @param vo
     * @return
     */
    @Transactional
    @Override
    public Boolean orderLockStock(WareSkuLockVO vo) {

        // 保存库存工作单的消息
        WareOrderTaskEntity taskEntity = new WareOrderTaskEntity();
        taskEntity.setOrderSn(vo.getOrderSn());
        orderTaskService.save(taskEntity);

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
                    // TODO 告诉MQ库存锁定成功
                    WareOrderTaskDetailEntity entity = new WareOrderTaskDetailEntity(null, skuId, "", hasStock.getNum(),
                            taskEntity.getId(), wareId, 1);
                    orderTaskDetailService.save(entity);

                    StockLockedTo lockedTo = new StockLockedTo();
                    lockedTo.setId(taskEntity.getId());
                    StockDetailTo stockDetailTo = new StockDetailTo();
                    BeanUtils.copyProperties(entity, stockDetailTo);
                    lockedTo.setDetail(stockDetailTo);

                    // 发送消息给交换机
                    rabbitTemplate.convertAndSend("stock-event-exchange", "stock.locked", lockedTo);
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

    @Override
    public void unlockStock(StockLockedTo to) {
        System.out.println("收到解锁库存的消息");
        /*
         *  首先查询这个订单的锁定库存详情信息
         *      有：库存锁定成功
         *          需要解锁的情况如下：
         *              1、没有查询到这个订单，说明订单回滚了，因此库存也需要解锁
         *              2、有这个订单，需要根据订单状态来判断是否解锁库存
         *                  订单状态：已取消，解锁库存
         *                           未取消，无需解锁
         *      没有：说明锁定库存失败了，库存工作单一起回滚了，所以这种情况无需处理
         */
        StockDetailTo detail = to.getDetail();
        Long detailId = detail.getId();
        WareOrderTaskDetailEntity detailEntity = orderTaskDetailService.getById(detailId);
        if (detailEntity != null) {
            // 工作单不为null，说明锁定库存成功了，此时需要判断订单的状态来决定是否解锁库存
            Long id = to.getId();
            WareOrderTaskEntity taskEntity = orderTaskService.getById(id);
            String orderSn = taskEntity.getOrderSn();
            // 根据订单号查询出订单
            R r = orderFeignService.getOrderStatus(orderSn);
            if (r.getCode() == 0) {
                // 订单数据返回成功
                OrderVO orderVO = r.getData(new TypeReference<OrderVO>() {
                });
                if (orderVO == null || orderVO.getStatus() == 4) {
                    // 订单被取消并且状态为1的时候，解锁库存
                    if (detailEntity.getLockStatus() == 1) {
                        unlockStock(detail.getSkuId(), detail.getWareId(), detail.getSkuNum());
                        // 解锁成功，手动ack
                    }
                }
            } else {
                // 远程查询失败，拒绝消息重新放到队列中
                throw new RuntimeException("远程服务失败");
            }

        } else {
            // 无需解锁
        }
    }

    /**
     * 订单关闭主动发消息准备解锁库存
     *
     * @param to
     */
    @Override
    public void unlockStock(OrderTo to) {
        String orderSn = to.getOrderSn();
        WareOrderTaskEntity taskEntity = orderTaskService
                .getOne(new QueryWrapper<WareOrderTaskEntity>().eq("order_sn", orderSn));
        Long taskId = taskEntity.getId();
        // 按照库存工作单找到未解锁的库存进行解锁
        List<WareOrderTaskDetailEntity> list = orderTaskDetailService.list(new QueryWrapper<WareOrderTaskDetailEntity>()
                .eq("task_id", taskId).eq("lock_status", 1));
        for (WareOrderTaskDetailEntity entity : list) {
            unlockStock(entity.getSkuId(), entity.getWareId(), entity.getSkuNum());
        }

    }

    @Data
    static class SkuWareHasStock {
        private Long skuId;
        private Integer num;
        private List<Long> wareIds;
    }

}