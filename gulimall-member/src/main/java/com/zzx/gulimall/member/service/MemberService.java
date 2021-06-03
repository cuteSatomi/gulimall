package com.zzx.gulimall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zzx.common.utils.PageUtils;
import com.zzx.gulimall.member.entity.MemberEntity;
import com.zzx.gulimall.member.exception.PhoneExistException;
import com.zzx.gulimall.member.exception.UsernameExistException;
import com.zzx.gulimall.member.vo.MemberLoginVO;
import com.zzx.gulimall.member.vo.MemberRegisterVO;
import com.zzx.gulimall.member.vo.SocialUser;

import java.util.Map;

/**
 * 会员
 *
 * @author zzx
 * @email hdsomedezzx@gmail.com
 * @date 2021-05-03 23:11:01
 */
public interface MemberService extends IService<MemberEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 会员注册
     * @param vo
     */
    void register(MemberRegisterVO vo);

    /**
     * 检查手机号码是否已被注册
     *
     * @param phone
     * @throws PhoneExistException
     */
    void checkPhoneUnique(String phone) throws PhoneExistException;

    /**
     * 检查用户名是否已经被注册
     *
     * @param username
     * @throws UsernameExistException
     */
    void checkUsernameUnique(String username) throws UsernameExistException;

    /**
     * 登录方法
     * @param vo
     * @return
     */
    MemberEntity login(MemberLoginVO vo);

    /**
     * 社交登录(微博登录)功能
     *
     * @param user
     * @return
     */
    MemberEntity login(SocialUser user);
}

