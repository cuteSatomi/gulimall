package com.zzx.gulimall.cart.to;

import lombok.Data;

/**
 * @author zzx
 * @date 2021-06-04 21:37
 */
@Data
public class UserInfoTO {
    private Long userId;
    private String userKey;
    private Boolean tempUser = false;
}
