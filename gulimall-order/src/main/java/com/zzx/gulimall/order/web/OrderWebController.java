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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
    public String submitOrder(OrderSubmitVO vo, Model model, RedirectAttributes redirectAttributes) {
        System.out.println("前端提交的数据是: " + vo);
        OrderSubmitResponseVO responseVO = orderService.submitOrder(vo);
        if (responseVO.getCode() == 0) {
            // 成功，来到支付选择项
            model.addAttribute("responseVO", responseVO);
            return "pay";
        } else {
            String msg = "下单失败， ";
            switch (responseVO.getCode()) {
                case 1:
                    msg += "订单信息过期，请刷新再次提交";
                    break;
                case 2:
                    msg += "订单商品价格发生变化，请确认后再次提交";
                    break;
                case 3:
                    msg += "库存锁定失败，商品库存不足";
                    break;
                default:
                    break;
            }
            redirectAttributes.addFlashAttribute("msg", msg);
            // 失败，返回确认页
            return "redirect:http://order.gulimall.com/toTrade";
        }
    }
}
