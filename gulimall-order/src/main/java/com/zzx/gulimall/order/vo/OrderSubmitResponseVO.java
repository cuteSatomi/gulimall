package com.zzx.gulimall.order.vo;

import com.zzx.gulimall.order.entity.OrderEntity;
import lombok.Data;

/**
 * @author zzx
 * @date 2021-06-10 16:31
 */
@Data
public class OrderSubmitResponseVO {
    private OrderEntity order;
    /** 状态码  0才是成功 */
    private Integer code;
}
