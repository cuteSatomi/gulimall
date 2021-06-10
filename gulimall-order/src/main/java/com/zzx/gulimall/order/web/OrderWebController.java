package com.zzx.gulimall.order.web;

import com.zzx.gulimall.order.service.OrderService;
import com.zzx.gulimall.order.vo.OrderConfirmVO;
import com.zzx.gulimall.order.vo.OrderSubmitResponseVO;
import com.zzx.gulimall.order.vo.OrderSubmitVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

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

    @PostMapping("/submitOrder")
    public String submitOrder(OrderSubmitVO vo) {
        System.out.println("前端提交的数据是: " + vo);
        OrderSubmitResponseVO responseVO = orderService.submitOrder(vo);
        if(responseVO.getCode() == 0){
            // 成功，来到支付选择项
            return "pay";
        }else {
            // 失败，返回确认页
            return "redirect:http://order.gulimall.com/toTrade";
        }
    }
}
