package com.zzx.gulimall.member.exception;

/**
 * @author zzx
 * @date 2021-05-31 21:05
 */
public class PhoneExistException extends RuntimeException {
    public PhoneExistException() {
        super("手机号已存在");
    }
}
