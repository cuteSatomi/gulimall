package com.zzx.gulimall.search.vo;

import lombok.Data;

import java.util.List;

/**
 * @author zzx
 * @date 2021-05-24 15:39
 */
@Data
public class SearchParam {
    /** 页面传递的全文匹配关键字 */
    private String keyword;
    /** 三级分类id */
    private Long catalog3Id;
    /** 排序条件 */
    private String sort;
    /** 是否有库存 */
    private Integer hasStock;
    /** 价格区间查询 */
    private String skuPrice;
    /** 品牌id */
    private List<String> brandId;
    /** 按照属性进行筛选 */
    private List<String> attrs;
    /** 当前页码 */
    private Integer pageNum;

}
