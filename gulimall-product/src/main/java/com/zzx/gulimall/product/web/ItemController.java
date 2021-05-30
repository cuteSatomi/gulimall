package com.zzx.gulimall.product.web;

import com.zzx.gulimall.product.service.SkuInfoService;
import com.zzx.gulimall.product.vo.web.SkuItemVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.concurrent.ExecutionException;

/**
 * @author zzx
 * @date 2021-05-30 14:49
 */
@Controller
public class ItemController {

    @Autowired
    private SkuInfoService skuInfoService;

    /**
     * 展示当前sku的详情
     *
     * @param skuId
     * @return
     */
    @GetMapping("/{skuId}.html")
    public String skuItem(@PathVariable Long skuId, Model model) throws ExecutionException, InterruptedException {

        System.out.println("准备查询" + skuId + "的详情");
        SkuItemVO skuItemVO = skuInfoService.item(skuId);
        model.addAttribute("item",skuItemVO);
        return "item";
    }
}
