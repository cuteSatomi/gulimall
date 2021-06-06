package com.zzx.gulimall.cart.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author zzx
 * @date 2021-05-30 21:36
 */
@Data
@Component
@ConfigurationProperties(prefix = "gulimall.config.thread")
public class ThreadPoolConfigProperties {
    private Integer corePoolSize;
    private Integer maximumPoolSize;
    private Long keepAliveTime;
}
