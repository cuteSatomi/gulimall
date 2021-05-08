package com.zzx.gulimall.product.config;

import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @author zzx
 * @date 2021-05-08 15:24:40
 */
@Configuration
@EnableTransactionManagement
@MapperScan(basePackages = "com.zzx.gulimall.product.dao")
public class MybatisConfig {

    @Bean
    public PaginationInterceptor paginationInterceptor() {
        PaginationInterceptor paginationInterceptor = new PaginationInterceptor();
        // 请求页面大于最大页数后的操作，true回到首页，false继续请求，默认false
        paginationInterceptor.setOverflow(true);
        // 每页最大显示条数
        paginationInterceptor.setLimit(500);
        return paginationInterceptor;
    }
}
