package com.asiainfo.frame.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author: Ares
 * @date: 2020/04/28 16:00
 * @description: 服务提供者注解
 * @version: JDK 1.8
 */

@Target({ElementType.CONSTRUCTOR, ElementType.METHOD, ElementType.PARAMETER, ElementType.FIELD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface AresConsumer
{
    /**
     * 服务中心名
     */
    String center();

    /**
     * 服务组别
     */
    String group();

    /**
     * 服务版本
     */
    String version();

    /**
     * 做兼容处理
     */
    boolean required() default false;
}
