package com.asiainfo.strategy.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;

/**
 * @author: Ares
 * @date: 2020/4/7 14:30
 * @description: 为了使spring-boot-starter-validation在业务层使用
 * 在接口类上使用@Validated, 在方法上使用校验注解(如@NotNull), 如果是实体想校验其内部的属性那么需要再加上@Valid, 实体中的实体也需要@Valid
 * @version: JDK 1.8
 */
@Configuration
public class ValidatorConfiguration
{
    @Bean
    public MethodValidationPostProcessor methodValidationPostProcessor()
    {
        return new MethodValidationPostProcessor();
    }
}
