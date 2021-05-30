package com.zzx.gulimall.product.vo.web;

import com.zzx.gulimall.product.entity.SkuImagesEntity;
import com.zzx.gulimall.product.entity.SkuInfoEntity;
import com.zzx.gulimall.product.entity.SpuInfoDescEntity;
import lombok.Data;

import java.util.List;

/**
 * @author zzx
 * @date 2021-05-30 15:04
 */
@Data
public class SkuItemVO {
    /** 1、sku基本信息获取，相关表：pms_sku_info */
    private SkuInfoEntity skuInfo;
    /** 2、sku的图片信息，相关表：pms_sku_images */
    private List<SkuImagesEntity> images;
    /** 3、spu的销售属性组合 */
    private List<SkuItemSaleAttrVO> saleAttr;
    /** 4、spu介绍 */
    private SpuInfoDescEntity desc;
    /** 5、spu的规格参数信息 */
    private List<SpuItemAttrGroupVO> groupAttrs;

    @Data
    public static class SkuItemSaleAttrVO {
        /** 属性id */
        private Long attrId;
        /** 属性名 */
        private String attrName;
        /** 属性值 */
        private List<AttrValueWithSkuIdVO> attrValues;
    }

    @Data
    public static class SpuItemAttrGroupVO {
        /** 基本属性分组名字 */
        private String groupName;
        private List<SpuBaseAttrVO> attrs;
    }

    @Data
    public static class SpuBaseAttrVO {
        /** 属性名 */
        private String attrName;
        /** 属性值 */
        private String attrValue;
    }

    @Data
    public static class AttrValueWithSkuIdVO {
        /** 属性名 */
        private String attrValue;
        /** 拥有该属性的skuId集合 */
        private String skuIds;
    }
}
