package com.zzx.gulimall.product.vo.response;

import com.zzx.gulimall.product.vo.AttrVO;
import lombok.Data;

/**
 * @author zzx
 * @date 2021-05-10 10:21:22
 */
@Data
public class AttrResponseVO extends AttrVO {

    /** 所属分类名字 */
    private String catelogName;
    /** 所属分组名字 */
    private String groupName;
    /** 分类id完整路径 */
    private Long[] catelogPath;
}
