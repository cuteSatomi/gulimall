package com.zzx.gulimall.search.vo;

import com.zzx.common.to.es.SkuEsModel;
import lombok.Data;

import java.util.List;

/**
 * @author zzx
 * @date 2021-05-24 16:00
 */
@Data
public class SearchResult {
    /** 查询到的所有商品信息 */
    private List<SkuEsModel> products;
    /** 当前页码 */
    private Integer pageNum;
    /** 总记录数 */
    private Long total;
    /** 总页数 */
    private Integer totalPages;

    /** 当前查询到的结果：所有涉及到的品牌 */
    private List<BrandVO> brands;
    /** 当前查询到的结果：所有涉及到的分类 */
    private List<CatalogVO> catalogs;
    /** 当前查询到的结果：所有涉及到的属性 */
    private List<AttrVO> attrs;

    /** 面包屑导航数据 */
    private List<NavVO> navs;

    @Data
    public static class NavVO {
        private String navName;
        private String navValue;
        private String link;
    }

    @Data
    public static class BrandVO {
        private Long brandId;
        private String brandName;
        private String brandImg;
    }

    @Data
    public static class CatalogVO {
        private Long catalogId;
        private String catalogName;
    }

    @Data
    public static class AttrVO {
        private Long attrId;
        private String attrName;
        private List<String> attrValue;
    }
}
