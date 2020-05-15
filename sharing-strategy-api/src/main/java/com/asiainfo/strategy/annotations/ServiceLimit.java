package com.asiainfo.strategy.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author: Ares
 * @date: 2020/5/14 21:07
 * @description: 服务限速注解
 * @version: JDK 1.8
 */

@Inherited
@Documented
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ServiceLimit
{
    /**
     * 调用限制次数
     */
    String permitTimes() default "100";

    /**
     * 时间周期 单位毫秒
     */
    String period() default "1000";
}
