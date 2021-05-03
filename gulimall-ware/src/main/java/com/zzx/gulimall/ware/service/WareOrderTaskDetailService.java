package com.zzx.gulimall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zzx.common.utils.PageUtils;
import com.zzx.gulimall.ware.entity.WareOrderTaskDetailEntity;

import java.util.Map;

/**
 * 库存工作单
 *
 * @author zzx
 * @email hdsomedezzx@gmail.com
 * @date 2021-05-03 23:26:34
 */
public interface WareOrderTaskDetailService extends IService<WareOrderTaskDetailEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

