package com.zzx.gulimall.cart.config;

import com.zzx.gulimall.cart.interceptor.CartInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author zzx
 * @date 2021-06-04 22:03
 */
@Configuration
public class GulimallWebConfig implements WebMvcConfigurer {
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 购物车所有请求的微服务都将先经过CartInterceptor拦截器
        registry.addInterceptor(new CartInterceptor()).addPathPatterns("/**");
    }
}
