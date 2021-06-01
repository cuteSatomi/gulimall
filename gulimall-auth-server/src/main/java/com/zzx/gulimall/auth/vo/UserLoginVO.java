package com.zzx.gulimall.auth.vo;

import lombok.Data;

import javax.validation.constraints.NotEmpty;

/**
 * @author zzx
 * @date 2021-06-01 9:52
 */
@Data
public class UserLoginVO {
    @NotEmpty(message = "用户名必须提交")
    private String loginAcct;
    @NotEmpty(message = "密码必须提交")
    private String password;
}
