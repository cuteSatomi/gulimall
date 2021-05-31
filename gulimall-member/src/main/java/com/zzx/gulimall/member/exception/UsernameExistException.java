package com.zzx.gulimall.member.exception;

/**
 * @author zzx
 * @date 2021-05-31 21:06
 */
public class UsernameExistException extends RuntimeException {
    public UsernameExistException() {
        super("用户名已存在");
    }
}
