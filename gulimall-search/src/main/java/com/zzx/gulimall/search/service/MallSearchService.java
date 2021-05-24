package com.zzx.gulimall.search.service;

import com.zzx.gulimall.search.vo.SearchParam;
import com.zzx.gulimall.search.vo.SearchResult;


/**
 * @author zzx
 * @date 2021-05-24 15:41
 */
public interface MallSearchService {
    /**
     * 检索的方法
     *
     * @param param 检索的所有参数
     * @return 返回检索的结果
     */
    SearchResult search(SearchParam param);
}
