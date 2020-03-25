package com.asiainfo.strategy.multiple.datasources;


import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * @author: Ares
 * @date: 2020/3/19 12:39
 * @description: 用于切换数据源的切面
 * Order为-1保证在事务开启前先执行切换
 * @version: JDK 1.8
 */
@Aspect
@Order(-1)
@Component
public class DynamicDataSourceAspect
{
    private static final Logger LOGGER = LoggerFactory.getLogger(DynamicDataSourceAspect.class);


    /**
     * @author: Ares
     * @description: 根据注解配置设置数据源, 方法共分为以下情况
     * 1.没有TargetDataSource注解, 使用默认数据源
     * 2.有TargetDataSource注解且不指定数据源, 使用默认数据源
     * 3.有TargetDataSource注解且指定存在的数据源, 使用指定数据源(可能是默认数据源)
     * 4.有TargetDataSource注解且指定不存在的数据源, 打印异常信息并使用默认数据源
     * @date: 2020/3/19 13:03
     * @param: [proceedingJoinPoint, targetDataSource] 请求参数
     * @return: java.lang.Object 响应参数
     */
    @Around("@annotation(targetDataSource)")
    public Object changeDataSource(ProceedingJoinPoint proceedingJoinPoint, TargetDataSource targetDataSource) throws Throwable
    {
        String dataSourceId = targetDataSource.dataSourceId();
        Signature signature = proceedingJoinPoint.getSignature();
        String methodName = signature.getDeclaringTypeName() + DynamicDataSourceConstans.CLASS_AND_METHOD_CONNECTOR + signature.getName();
        DynamicDataSourceUtil.changeDataSource(dataSourceId, methodName);

        Object result = proceedingJoinPoint.proceed();

        LOGGER.debug("方法{}结束, 清理数据源标识: {}", methodName, dataSourceId);
        DynamicDataSourceUtil.clearDataSourceId();

        return result;
    }

}
