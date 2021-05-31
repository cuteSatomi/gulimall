package com.zzx.gulimall.member.dao;

import com.zzx.gulimall.member.entity.MemberLevelEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员等级
 *
 * @author zzx
 * @email hdsomedezzx@gmail.com
 * @date 2021-05-03 23:11:00
 */
@Mapper
public interface MemberLevelDao extends BaseMapper<MemberLevelEntity> {

    /**
     * 查询默认会员等级
     *
     * @return
     */
    MemberLevelEntity getDefaultLevel();
}
