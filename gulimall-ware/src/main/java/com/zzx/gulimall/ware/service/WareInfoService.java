package com.zzx.gulimall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zzx.common.utils.PageUtils;
import com.zzx.gulimall.ware.entity.WareInfoEntity;
import com.zzx.gulimall.ware.vo.FareVO;

import java.util.Map;

/**
 * 仓库信息
 *
 * @author zzx
 * @email hdsomedezzx@gmail.com
 * @date 2021-05-03 23:26:34
 */
public interface WareInfoService extends IService<WareInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 根据地址查询运费
     * @param addrId
     * @return
     */
    FareVO getFare(Long addrId);
}

