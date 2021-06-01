package com.zzx.gulimall.member.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zzx.common.utils.PageUtils;
import com.zzx.common.utils.Query;
import com.zzx.gulimall.member.dao.MemberDao;
import com.zzx.gulimall.member.entity.MemberEntity;
import com.zzx.gulimall.member.entity.MemberLevelEntity;
import com.zzx.gulimall.member.exception.PhoneExistException;
import com.zzx.gulimall.member.exception.UsernameExistException;
import com.zzx.gulimall.member.service.MemberLevelService;
import com.zzx.gulimall.member.service.MemberService;
import com.zzx.gulimall.member.vo.MemberLoginVO;
import com.zzx.gulimall.member.vo.MemberRegisterVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;


@Service("memberService")
public class MemberServiceImpl extends ServiceImpl<MemberDao, MemberEntity> implements MemberService {

    @Autowired
    private MemberLevelService memberLevelService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<MemberEntity> page = this.page(
                new Query<MemberEntity>().getPage(params),
                new QueryWrapper<MemberEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 会员注册
     *
     * @param vo
     */
    @Override
    public void register(MemberRegisterVO vo) {
        MemberEntity member = new MemberEntity();
        // 查询出默认的会员等级
        MemberLevelEntity defaultLevel = memberLevelService.getOne(
                new QueryWrapper<MemberLevelEntity>().eq("default_status", 1));
        //3.3.2 设置会员等级为默认
        member.setLevelId(defaultLevel.getId());

        // 检查手机号码和用户名
        checkPhoneUnique(vo.getPhone());
        checkUsernameUnique(vo.getUsername());

        // 设置一些必要的字段
        member.setUsername(vo.getUsername());
        member.setMobile(vo.getPhone());
        // 密码需要加密
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String encodePassword = passwordEncoder.encode(vo.getPassword());
        member.setPassword(encodePassword);

        baseMapper.insert(member);
    }

    @Override
    public void checkPhoneUnique(String phone) throws PhoneExistException {
        Integer count = baseMapper.selectCount(new QueryWrapper<MemberEntity>().eq("mobile", phone));
        if (count > 0) {
            throw new PhoneExistException();
        }
    }

    @Override
    public void checkUsernameUnique(String username) throws UsernameExistException {
        Integer count = baseMapper.selectCount(new QueryWrapper<MemberEntity>().eq("username", username));
        if (count > 0) {
            throw new UsernameExistException();
        }
    }

    /**
     * 登录
     * @param vo
     * @return
     */
    @Override
    public MemberEntity login(MemberLoginVO vo) {
        String loginAcct = vo.getLoginAcct();
        String password = vo.getPassword();

        // 查询用户
        MemberEntity memberEntity = baseMapper.selectOne(
                new QueryWrapper<MemberEntity>().eq("username", loginAcct).or().eq("mobile", loginAcct));
        if (memberEntity == null) {
            // 登录失败
            return null;
        }else {
            String passwordDb = memberEntity.getPassword();
            BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            boolean matches = passwordEncoder.matches(password, passwordDb);
            if (matches) {
                return memberEntity;
            }else {
                return null;
            }
        }
    }
}