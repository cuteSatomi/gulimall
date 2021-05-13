package com.zzx.gulimall.ware.vo.request;

import lombok.Data;

import java.util.List;

@Data
public class MergePurchaseVO {
    private Long purchaseId;
    private List<Long> items;
}
