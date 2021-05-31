package com.zzx.gulimall.auth.vo;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

/**
 * @author zzx
 * @date 2021-05-31 16:43
 */
@Data
public class UserRegisterVO {
    @NotEmpty(message = "用户名必须提交")
    private String username;
    @NotEmpty(message = "密码必须提交")
    private String password;
    @Pattern(regexp = "^[1]([3-9])[0-9]{0}$", message = "手机号格式不正确")
    private String phone;
    @NotEmpty(message = "验证码必须提交")
    private String code;
}
