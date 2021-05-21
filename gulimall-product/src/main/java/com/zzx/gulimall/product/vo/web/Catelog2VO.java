package com.zzx.gulimall.product.vo.web;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author zzx
 * @date 2021-05-21 16:15
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Catelog2VO {
    private String catalog1Id;
    private List<Catelog3VO> catalog3List;
    private String id;
    private String name;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Catelog3VO {
        private String catalog2Id;
        private String id;
        private String name;
    }
}
