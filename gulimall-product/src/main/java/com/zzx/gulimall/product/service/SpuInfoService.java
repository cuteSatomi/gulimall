package com.zzx.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zzx.common.utils.PageUtils;
import com.zzx.gulimall.product.entity.SpuInfoEntity;
import com.zzx.gulimall.product.vo.request.SpuSaveVO;

import java.util.Map;

/**
 * spu信息
 *
 * @author zzx
 * @email hdsomedezzx@gmail.com
 * @date 2021-05-03 22:26:54
 */
public interface SpuInfoService extends IService<SpuInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 保存spu
     *
     * @param vo
     */
    void saveSpuInfo(SpuSaveVO vo);

    /**
     * 保存spu基本信息
     * @param infoEntity
     */
    void saveBaseSpuInfo(SpuInfoEntity infoEntity);

}

