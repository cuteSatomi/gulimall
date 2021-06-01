package com.zzx.gulimall.auth.controller;

import com.alibaba.fastjson.TypeReference;
import com.zzx.common.constant.AuthServerConstant;
import com.zzx.common.gulienum.BizCodeEnum;
import com.zzx.common.utils.R;
import com.zzx.gulimall.auth.feign.MemberFeignService;
import com.zzx.gulimall.auth.vo.UserLoginVO;
import com.zzx.gulimall.auth.vo.UserRegisterVO;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author zzx
 * @date 2021-05-31 10:18
 */
@Controller
public class LoginController {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private MemberFeignService memberFeignService;

    @GetMapping("/sms/sendcode")
    @ResponseBody
    public R sendCode(@RequestParam("phone") String phone) {
        // TODO 接口防刷

        String key = AuthServerConstant.SMS_CODE_CACHE_PREFIX + phone;
        String redisCode = redisTemplate.opsForValue().get(key);
        if (StringUtils.isBlank(redisCode)) {
            // redis中没有该手机号码，则发送短信,redis缓存验证码五分钟有效
            String code = UUID.randomUUID().toString().substring(0, 5) + "_" + System.currentTimeMillis();
            redisTemplate.opsForValue().set(key, code, 5, TimeUnit.MINUTES);
        } else {
            // redis中已经存了该手机号码的验证码
            long sendTime = Long.parseLong(redisCode.split("_")[1]);
            if (System.currentTimeMillis() - sendTime < 60000) {
                // 60秒内不能再发
                return R.error(BizCodeEnum.SMS_CODE_EXCEPTION.getCode(), BizCodeEnum.SMS_CODE_EXCEPTION.getMsg());
            }
        }

        return R.ok();
    }

    @PostMapping("/register")
    public String register(@Valid UserRegisterVO vo, BindingResult result, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            Map<String, String> errors = result.getFieldErrors().stream().collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));
            redirectAttributes.addFlashAttribute("errors", errors);
            // 校验出错回到注册页面
            return "redirect:http://auth.gulimall.com/reg.html";
        }

        // 调用远程服务进行注册
        String code = vo.getCode();
        String key = AuthServerConstant.SMS_CODE_CACHE_PREFIX + vo.getPhone();
        String redisCode = redisTemplate.opsForValue().get(key);
        if(StringUtils.isNotBlank(redisCode)){
            // 如果redis中的验证码不为空，校验验证码是否正确
            if(code.equals(redisCode.split("_")[0])){
                // 校验通过，删除redis中的验证码
                redisTemplate.delete(key);
                // 调用远程服务注册
                R r = memberFeignService.register(vo);
                if(r.getCode().equals(0)){
                    // 成功，返回登陆页面
                    return "redirect:http://auth.gulimall.com/login.html";
                }else {
                    Map<String,String> errors = new HashMap<>();
                    errors.put("msg",r.getData("msg",new TypeReference<String>(){}));
                    redirectAttributes.addFlashAttribute("errors",errors);
                    // 失败，返回注册页面
                    return "redirect:http://auth.gulimall.com/reg.html";
                }
            }else {
                // 校验不通过，返回注册页面
                Map<String, String> errors = new HashMap<>();
                errors.put("code","验证码错误");
                redirectAttributes.addFlashAttribute("errors", errors);
                // 校验出错回到注册页面
                return "redirect:http://auth.gulimall.com/reg.html";
            }
        }else {
            // redis中验证码为空，返回注册页面
            Map<String, String> errors = new HashMap<>();
            errors.put("code","验证码错误");
            redirectAttributes.addFlashAttribute("errors", errors);
            // 校验出错回到注册页面
            return "redirect:http://auth.gulimall.com/reg.html";
        }
    }

    @PostMapping("/login")
    public String login(UserLoginVO vo,RedirectAttributes redirectAttributes){
        // 远程登录
        R r = memberFeignService.login(vo);
        if (r.getCode().equals(0)) {
            // 成功
            return "redirect:http://gulimall.com";
        }else {
            Map<String,String> errors = new HashMap<>();
            errors.put("msg",r.getData("msg",new TypeReference<String>(){}));
            redirectAttributes.addFlashAttribute("errors",errors);
            return "redirect:http://auth.gulimall.com/login.html";
        }
    }
}
