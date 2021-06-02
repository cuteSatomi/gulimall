package com.zzx.gulimall.auth.controller;

import com.zzx.common.utils.HttpUtils;
import com.zzx.gulimall.auth.feign.MemberFeignService;
import org.apache.http.HttpResponse;
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
        System.out.println(response.getStatusLine().getStatusCode());
        if (response.getStatusLine().getStatusCode() == 200) {
            // 使用code获取token成功
        }
        return "redirect:http://auth.gulimall.com/login.html";
    }
}
