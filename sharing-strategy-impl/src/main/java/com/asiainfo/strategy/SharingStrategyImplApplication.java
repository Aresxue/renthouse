package com.asiainfo.strategy;

import com.asiainfo.frame.annotations.AresComponentScan;
import com.asiainfo.strategy.service.ConditionService;
import com.asiainfo.strategy.service.impl.ConditionServiceRemoteImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.ConfigurableApplicationContext;


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
    private static final Logger LOGGER = LoggerFactory.getLogger(SharingStrategyImplApplication.class);

    public static void main(String[] args)
    {
        SpringApplication.run(SharingStrategyImplApplication.class, args);
        LOGGER.info("合租攻略服务启动成功");
    }

}
