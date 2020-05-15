package com.asiainfo.strategy;

import com.asiainfo.frame.annotations.AresComponentScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;


/**
 * @author: Ares
 * @date: 2020/3/25 15:09
 * @description: 启动主类
 * @version: JDK 1.8
 */
@SpringBootApplication(scanBasePackages = {"com.asiainfo.frame", "com.asiainfo.strategy.service.impl", "com.asiainfo.strategy.module", "com.asiainfo.strategy.dao", "com.asiainfo.strategy.mapper", "com.asiainfo.strategy.config", "com.asiainfo.strategy.aop"})
@EnableEurekaClient
@AresComponentScan(basePackages = "com.asiainfo.strategy.service.impl")
public class SharingStrategyImplApplication
{

    public static void main(String[] args)
    {
        SpringApplication.run(SharingStrategyImplApplication.class, args);
    }

}
