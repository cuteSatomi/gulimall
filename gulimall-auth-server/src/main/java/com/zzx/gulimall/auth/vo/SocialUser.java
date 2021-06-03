package com.zzx.gulimall.auth.vo;

import lombok.Data;

/**
 * @author zzx
 * @date 2021-06-03 14:23
 */
@Data
public class SocialUser {
    private String accessToken;
    private String remindIn;
    private String expiresIn;
    private String uid;
    private String isRealName;

}
