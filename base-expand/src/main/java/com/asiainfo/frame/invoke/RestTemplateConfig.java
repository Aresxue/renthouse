package com.asiainfo.frame.invoke;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * @author: Ares
 * @date: 2019/6/11 16:37
 * @description: RestTemplate配置类
 * @version: JDK 1.8
 */
@Configuration
public class RestTemplateConfig
{
    @Autowired
    private RestTemplateBuilder builder;

    @LoadBalanced
    @Bean
    public RestTemplate restTemplate()
    {
        return builder.build();
    }

}
