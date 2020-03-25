package com.asiainfo.strategy.multiple.datasources;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author: Ares
 * @date: 2020/3/19 11:56
 * @description: 动态数据源路由注解
 * 在方法上使用, 用于指定使用哪个数据源
 * @version: JDK 1.8
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface TargetDataSource
{
    String dataSourceId() default DynamicDataSourceConstans.DEFAULT_TARGET_DATASOURCE;
}
