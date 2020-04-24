package com.asiainfo.strategy;

import com.asiainfo.strategy.config.HttpConnectionPoolConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.Import;


/**
 * @author: Ares
 * @date: 2020/3/25 15:09
 * @description: 启动主类
 * @version: JDK 1.8
 */
@SpringBootApplication(scanBasePackages = {
        "com.asiainfo.frame",
        "com.asiainfo.strategy.function.impl",
        "com.asiainfo.strategy.business.impl",
        "com.asiainfo.strategy.config",
        "com.asiainfo.strategy.multiple.datasources",
        "com.asiainfo.strategy.mapper",
        "com.asiainfo.strategy.aop"})
@EnableEurekaClient
@Import({HttpConnectionPoolConfig.class})
public class SharingStrategyImplApplication
{

    public static void main(String[] args)
    {
        SpringApplication.run(SharingStrategyImplApplication.class, args);
    }

}
