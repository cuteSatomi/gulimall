package com.zzx.gulimall.order.feign;

import com.zzx.gulimall.order.vo.OrderItemVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

/**
 * @author zzx
 * @date 2021-06-09 20:32
 */
@FeignClient("gulimall-cart")
public interface CartFeignService {

    @GetMapping("/currentUserCartItems")
    List<OrderItemVO> getCurrentUserCartItems();
}
