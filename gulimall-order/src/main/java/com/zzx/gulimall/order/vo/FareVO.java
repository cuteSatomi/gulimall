package com.zzx.gulimall.order.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author zzx
 * @date 2021-06-10 15:41
 */
@Data
public class FareVO {
    private MemberAddressVO address;
    private BigDecimal fare;
}
