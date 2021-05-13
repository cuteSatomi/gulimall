package com.zzx.gulimall.ware.vo.request;

import lombok.Data;

/**
 * @author zzx
 * @date 2021-05-13 16:46
 */
@Data
public class PurchaseDetailDoneVO {
    private Long itemId;
    private Integer status;
    private String reason;
}
