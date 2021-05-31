package com.zzx.gulimall.auth.controller;

import com.zzx.common.constant.AuthServerConstant;
import com.zzx.common.gulienum.BizCodeEnum;
import com.zzx.common.utils.R;
import com.zzx.gulimall.auth.vo.UserRegisterVO;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.validation.Valid;
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
    public String register(@Valid UserRegisterVO vo, BindingResult result, Model model) {
        if (result.hasErrors()) {
            Map<String, String> errors = result.getFieldErrors().stream().collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));
            model.addAttribute("errors", errors);
            // 校验出错回到注册页面
            return "forward:/reg.html";
        }

        // 调用远程服务进行注册

        return "redirect:/login.html";
    }
}
