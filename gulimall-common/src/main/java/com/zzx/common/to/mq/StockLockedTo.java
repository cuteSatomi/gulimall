package com.zzx.common.to.mq;

import lombok.Data;

import java.io.Serializable;

/**
 * @author zzx
 * @date 2021-06-13 09:38
 */
@Data
public class StockLockedTo implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 库存工作单id */
    private Long id;
    /** 工作单详情的id */
    private StockDetailTo detail;
}
