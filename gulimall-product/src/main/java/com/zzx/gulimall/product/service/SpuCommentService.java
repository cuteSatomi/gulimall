package com.zzx.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zzx.common.utils.PageUtils;
import com.zzx.gulimall.product.entity.SpuCommentEntity;

import java.util.Map;

/**
 * 商品评价
 *
 * @author zzx
 * @email hdsomedezzx@gmail.com
 * @date 2021-05-03 22:26:54
 */
public interface SpuCommentService extends IService<SpuCommentEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

