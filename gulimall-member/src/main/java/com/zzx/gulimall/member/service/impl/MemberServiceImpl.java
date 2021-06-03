package com.zzx.gulimall.member.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zzx.common.utils.HttpUtils;
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
import com.zzx.gulimall.member.vo.SocialUser;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
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
     *
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
        } else {
            String passwordDb = memberEntity.getPassword();
            BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            boolean matches = passwordEncoder.matches(password, passwordDb);
            if (matches) {
                return memberEntity;
            } else {
                return null;
            }
        }
    }

    /**
     * 社交登录(微博登录)功能
     *
     * @param user
     * @return
     */
    @Override
    public MemberEntity login(SocialUser user) {
        // 首先根据uid到库中查询数据
        MemberEntity memberByUid = baseMapper.selectOne(new QueryWrapper<MemberEntity>().eq("uid", user.getUid()));
        if (memberByUid == null) {
            // 如果不存在，则需要根据token和uid向微博请求用户数据，并且自动注册
            Map<String, String> query = new HashMap<>();
            query.put("access_token", user.getAccessToken());
            query.put("uid", user.getUid());

            String json = "";
            try {
                HttpResponse response = HttpUtils.doGet("https://api.weibo.com", "/2/users/show.json", "get", new HashMap<>(), query);
                json = EntityUtils.toString(response.getEntity());
            } catch (Exception e) {
                e.printStackTrace();
            }
            JSONObject jsonObject = JSON.parseObject(json);
            // 头像
            String avatarLarge = jsonObject.getString("avatar_large");
            String gender = jsonObject.getString("gender");

            MemberLevelEntity defaultLevel = memberLevelService.getOne(new QueryWrapper<MemberLevelEntity>().eq("default_status", 1));

            // 赋一些初始值
            MemberEntity member = new MemberEntity();
            member.setUid(user.getUid());
            member.setRemindIn(user.getRemindIn());
            member.setExpiresIn(user.getExpiresIn());
            member.setAccessToken(user.getAccessToken());
            member.setCity(jsonObject.getString("location"));
            member.setNickname(jsonObject.getString("name"));
            member.setCreateTime(new Date());
            member.setLevelId(defaultLevel.getId());
            member.setGender("m".equals(gender) ? 0 : 1);
            member.setHeader(avatarLarge);
            // 保存用户
            save(member);

            return member;

        } else {
            // 如果库中已经存在该用户，则更新token信息并且返回
            memberByUid.setAccessToken(user.getAccessToken());
            memberByUid.setExpiresIn(user.getExpiresIn());
            memberByUid.setRemindIn(user.getRemindIn());
            updateById(memberByUid);
            return memberByUid;
        }
    }
}