package com.zzx.common.constant;

/**
 * @author zzx
 * @date 2021-05-10 14:35:10
 */
public class ProductConstant {
    public enum AttrEnum {
        /** 基本属性 */
        ATTR_TYPE_BASE(1, "基本属性"),
        /** 销售属性 */
        ATTR_TYPE_SALE(0, "销售属性");

        private Integer code;
        private String msg;

        AttrEnum(Integer code, String msg) {
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
}
