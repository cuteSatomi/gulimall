package com.zzx.gulimall.order.web;

import com.zzx.gulimall.order.service.OrderService;
import com.zzx.gulimall.order.vo.OrderConfirmVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.concurrent.ExecutionException;

/**
 * @author zzx
 * @date 2021-06-09 15:47
 */
@Controller
public class OrderWebController {

    @Autowired
    private OrderService orderService;

    @GetMapping("/toTrade")
    public String toTrade(Model model) throws ExecutionException, InterruptedException {
        OrderConfirmVO confirmVO = orderService.confirmOrder();
        model.addAttribute("orderConfirmData", confirmVO);
        return "confirm";
    }
}
