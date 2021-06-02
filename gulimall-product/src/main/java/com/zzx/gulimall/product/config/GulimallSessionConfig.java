package com.zzx.gulimall.product.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;

/**
 * @author zzx
 * @date 2021-06-02 13:57
 */
@Configuration
public class GulimallSessionConfig {
    @Bean
    public CookieSerializer  cookieSerializer(){
        DefaultCookieSerializer defaultCookieSerializer = new DefaultCookieSerializer();
        defaultCookieSerializer.setDomainName("gulimall.com");
        defaultCookieSerializer.setCookieName("GULISESSION");
        return defaultCookieSerializer;
    }

    @Bean
    public RedisSerializer<Object> springSessionDefaultReidsSerializer(){
        return new GenericJackson2JsonRedisSerializer();
    }
}
