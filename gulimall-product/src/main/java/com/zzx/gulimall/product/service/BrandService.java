package com.zzx.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zzx.common.utils.PageUtils;
import com.zzx.gulimall.product.entity.BrandEntity;

import java.util.Map;

/**
 * 品牌
 *
 * @author zzx
 * @email hdsomedezzx@gmail.com
 * @date 2021-05-03 22:26:54
 */
public interface BrandService extends IService<BrandEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 更新brand的细节，包括其他表中关于brand表的冗余字段
     * @param brand
     */
    void updateDetail(BrandEntity brand);
}

