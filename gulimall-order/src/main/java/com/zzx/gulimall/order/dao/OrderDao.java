package com.zzx.gulimall.order.dao;

import com.zzx.gulimall.order.entity.OrderEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单
 * 
 * @author zzx
 * @email hdsomedezzx@gmail.com
 * @date 2021-05-03 23:22:16
 */
@Mapper
public interface OrderDao extends BaseMapper<OrderEntity> {
	
}
