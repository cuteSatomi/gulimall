package com.zzx.gulimall.ware.vo.request;

import lombok.Data;

import java.util.List;


@Data
public class PurchaseDoneVO {
    private Long id;
    private List<PurchaseDetailDoneVO> items;
}
