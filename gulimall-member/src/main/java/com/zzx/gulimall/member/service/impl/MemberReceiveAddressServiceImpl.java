package com.zzx.gulimall.member.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zzx.common.utils.PageUtils;
import com.zzx.common.utils.Query;
import com.zzx.gulimall.member.dao.MemberReceiveAddressDao;
import com.zzx.gulimall.member.entity.MemberReceiveAddressEntity;
import com.zzx.gulimall.member.service.MemberReceiveAddressService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;


@Service("memberReceiveAddressService")
public class MemberReceiveAddressServiceImpl extends ServiceImpl<MemberReceiveAddressDao, MemberReceiveAddressEntity> implements MemberReceiveAddressService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<MemberReceiveAddressEntity> page = this.page(
                new Query<MemberReceiveAddressEntity>().getPage(params),
                new QueryWrapper<MemberReceiveAddressEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 根据会员id查询出该会员所有的收货地址
     *
     * @param memberId
     * @return
     */
    @Override
    public List<MemberReceiveAddressEntity> getAddresses(Long memberId) {
        return baseMapper.selectList(new QueryWrapper<MemberReceiveAddressEntity>().eq("member_id", memberId));
    }

}