package com.zzx.gulimall.member.dao;

import com.zzx.gulimall.member.entity.MemberEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员
 * 
 * @author zzx
 * @email hdsomedezzx@gmail.com
 * @date 2021-05-03 23:11:01
 */
@Mapper
public interface MemberDao extends BaseMapper<MemberEntity> {
	
}
