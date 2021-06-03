package com.zzx.gulimall.auth.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.zzx.common.constant.AuthServerConstant;
import com.zzx.common.utils.HttpUtils;
import com.zzx.common.utils.R;
import com.zzx.common.vo.MemberResponseVO;
import com.zzx.gulimall.auth.feign.MemberFeignService;
import com.zzx.gulimall.auth.vo.SocialUser;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

/**
 * @author zzx
 * @date 2021-06-02 20:56
 */
@Controller
public class OauthController {

    @Autowired
    private MemberFeignService memberFeignService;

    @Value("${spring.cloud.login.weibo.client-secret}")
    private String clientSecret;

    /**
     * 微博登陆成功以后的回调接口
     *
     * @param code
     * @param session
     * @return
     */
    @GetMapping("/oauth2/weibo/success")
    public String auth(@RequestParam("code") String code, HttpSession session) throws Exception {
        Map<String, String> query = new HashMap<>();
        query.put("client_id", "2758118393");
        query.put("client_secret", clientSecret);
        query.put("grant_type", "authorization_code");
        query.put("code", code);
        query.put("redirect_uri", "http://auth.gulimall.com/oauth2/weibo/success");
        HttpResponse response = HttpUtils.doPost("https://api.weibo.com", "/oauth2/access_token",
                "post", new HashMap<String, String>(), query, new HashMap<String, String>());
        Map<String, String> errors = new HashMap<>();
        if (response.getStatusLine().getStatusCode() == 200) {
            // 使用code获取token成功，获取token以及uid等信息
            String json = EntityUtils.toString(response.getEntity());
            SocialUser socialUser = JSON.parseObject(json, new TypeReference<SocialUser>() {
            });
            R r = memberFeignService.login(socialUser);
            if (r.getCode().equals(0)) {
                // 成功
                MemberResponseVO data = r.getData("data", new TypeReference<MemberResponseVO>() {
                });
                session.setAttribute(AuthServerConstant.LOGIN_USER, data);
                return "redirect:http://gulimall.com";
            } else {
                //2.2 否则返回登录页
                errors.put("msg", "登录失败，请重试");
                session.setAttribute("errors", errors);
                return "redirect:http://auth.gulimall.com/login.html";
            }
        }else {
            errors.put("msg", "获得第三方授权失败，请重试");
            session.setAttribute("errors", errors);
            return "redirect:http://auth.gulimall.com/login.html";
        }
    }
}
