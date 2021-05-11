package com.zzx.gulimall.product.vo.response;

import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.zzx.gulimall.product.entity.AttrEntity;
import lombok.Data;

import java.util.List;

/**
 * @author zzx
 * @date 2021-05-11 15:07:16
 */
@Data
public class AttrGroupWithAttrsVO {
    /**
     * 分组id
     */
    @TableId
    private Long attrGroupId;
    /**
     * 组名
     */
    private String attrGroupName;
    /**
     * 排序
     */
    private Integer sort;
    /**
     * 描述
     */
    private String descript;
    /**
     * 组图标
     */
    private String icon;
    /**
     * 所属分类id
     */
    private Long catelogId;

    private List<AttrEntity> attrs;
}
