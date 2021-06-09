package com.zzx.gulimall.order.interceptor;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * @author zzx
 * @date 2021-06-09 21:17
 */
@Component
public class GuliFeignInterceptor {

    @Bean("requestInterceptor")
    public RequestInterceptor requestInterceptor(){
        return template -> {
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            HttpServletRequest request = attributes.getRequest();
            if(request!=null){
                String cookie = request.getHeader("Cookie");
                // 为spring生成的feign请求加上cookie
                template.header("Cookie",cookie);
            }
        };
    }
}
