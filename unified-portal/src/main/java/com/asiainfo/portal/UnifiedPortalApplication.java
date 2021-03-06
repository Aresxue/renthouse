package com.asiainfo.portal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@SpringBootApplication(scanBasePackages = {"com.asiainfo.frame", "com.asiainfo.strategy", "com.asiainfo.portal"})
@EnableEurekaClient
public class UnifiedPortalApplication
{
    public static void main(String[] args)
    {
        SpringApplication.run(UnifiedPortalApplication.class, args);
    }

}
