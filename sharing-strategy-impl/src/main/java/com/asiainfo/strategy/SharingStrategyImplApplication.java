package com.asiainfo.strategy;

import com.asiainfo.strategy.config.HttpConnectionPoolConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.Import;

@SpringBootApplication(scanBasePackages = {"com.asiainfo.frame", "com.asiainfo.strategy"})
@EnableEurekaClient
@Import(HttpConnectionPoolConfig.class)
public class SharingStrategyImplApplication
{

    public static void main(String[] args)
    {
        SpringApplication.run(SharingStrategyImplApplication.class, args);
    }

}
