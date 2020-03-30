package com.asiainfo.strategy.multiple.datasources;

import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.mybatis.spring.transaction.SpringManagedTransaction;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.Properties;

/**
 * @author: Ares
 * @date: 2020/3/30 16:40
 * @description: 动态数据源切面
 * 拦截sql执行
 * @version: JDK 1.8
 */
@Component
@Intercepts({@Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class}), @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class})})
public class DynamicSourceInterceptor implements Interceptor
{
    /**
     * @author: Ares
     * @description: 强行把链接置空, 然后重新获取
     * @date: 2020/3/30 17:30
     * @param: [invocation] 请求参数
     * @return: java.lang.Object 响应参数
     */
    @Override
    public Object intercept(Invocation invocation) throws Throwable
    {
        // 如果数据源改变了那么将当前事务的链接置空以便重新获取
        // 第一次初始化时不置空
        if (null != DynamicDataSourceUtil.getDatasourceChange() && DynamicDataSourceUtil.getDatasourceChange())
        {
            Executor executor = (Executor) invocation.getTarget();
            SpringManagedTransaction springManagedTransaction = (SpringManagedTransaction) executor.getTransaction();
            Field connection = ReflectionUtils.findField(SpringManagedTransaction.class, "connection");
            if (null != connection)
            {
                connection.setAccessible(true);
                connection.set(springManagedTransaction, null);
            }
            // 只改变一次避免反射影响性能
            DynamicDataSourceUtil.setDatasourceChange(false);
        }
        return invocation.proceed();
    }

    @Override
    public Object plugin(Object target)
    {
        if (target instanceof Executor)
        {
            return Plugin.wrap(target, this);
        }
        else
        {
            return target;
        }
    }

    @Override
    public void setProperties(Properties properties)
    {

    }
}
