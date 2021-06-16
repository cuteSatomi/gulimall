package com.zzx.gulimall.coupon.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zzx.common.utils.PageUtils;
import com.zzx.common.utils.Query;
import com.zzx.gulimall.coupon.dao.SeckillSessionDao;
import com.zzx.gulimall.coupon.entity.SeckillSessionEntity;
import com.zzx.gulimall.coupon.service.SeckillSessionService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;


@Service("seckillSessionService")
public class SeckillSessionServiceImpl extends ServiceImpl<SeckillSessionDao, SeckillSessionEntity> implements SeckillSessionService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SeckillSessionEntity> page = this.page(
                new Query<SeckillSessionEntity>().getPage(params),
                new QueryWrapper<SeckillSessionEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 获取最近三天需要上架的秒杀商品
     *
     * @return
     */
    @Override
    public List<SeckillSessionEntity> getLatest3DaysSession() {
        List<SeckillSessionEntity> list = list(new QueryWrapper<SeckillSessionEntity>().between("start_time", startTime(), endTime()));
        return list;
    }

    private String startTime() {
        LocalDate now = LocalDate.now();
        LocalTime min = LocalTime.MIN;
        LocalDateTime dateTime = LocalDateTime.of(now, min);
        return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    private String endTime() {
        LocalDate now = LocalDate.now();
        LocalDate endDate = now.plusDays(2);
        LocalDateTime of = LocalDateTime.of(endDate, LocalTime.MAX);
        return of.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}