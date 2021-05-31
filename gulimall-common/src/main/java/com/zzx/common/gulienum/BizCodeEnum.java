package com.zzx.common.gulienum;

/**
 * @author zzx
 * @date 2021-05-06 21:23
 */
public enum BizCodeEnum {
    /** 系统未知异常 */
    UNKNOWN_EXCEPTION(10000, "系统未知异常"),
    /** 格式校验失败的异常 */
    VALID_EXCEPTION(10001, "格式校验失败"),
    SMS_CODE_EXCEPTION(10002, "验证码获取频率太高，请一分钟以后再试"),
    PRODUCT_UP_EXCEPTION(11000, "商品上架出现错误");

    /** 状态码 */
    private Integer code;
    /** 提示信息 */
    private String msg;

    BizCodeEnum(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public Integer getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
