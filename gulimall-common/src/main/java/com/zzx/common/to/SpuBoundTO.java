package com.zzx.common.to;

import lombok.Data;

import java.math.BigDecimal;

/**
 * spu的积分信息
 *
 * @author zzx
 * @date 2021-05-12 10:29:58
 */
@Data
public class SpuBoundTO {
    private Long spuId;
    private BigDecimal buyBounds;
    private BigDecimal growBounds;
}
