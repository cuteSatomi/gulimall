package com.zzx.gulimall.product.web;

import com.zzx.gulimall.product.entity.CategoryEntity;
import com.zzx.gulimall.product.service.CategoryService;
import com.zzx.gulimall.product.vo.web.Catelog2VO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

/**
 * @author zzx
 * @date 2021-05-21 15:42
 */
@Controller
public class IndexController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping({"/", "/index.html"})
    public String indexPage(Model model) {
        // 获取所有一级分类
        List<CategoryEntity> entities = categoryService.getLevel1Categories();
        model.addAttribute("categories",entities);
        return "index";
    }

    @ResponseBody
    @GetMapping("/index/catalog.json")
    public Map<String, List<Catelog2VO>> getCatalogJson(){
        Map<String, List<Catelog2VO>> map = categoryService.getCatalogJson();
        return map;
    }
}
