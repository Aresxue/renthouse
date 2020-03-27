package com.asiainfo.strategy.multiple.datasources;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.JdbcTransactionObjectSupport;
import org.springframework.transaction.TransactionDefinition;

/**
 * @author: Ares
 * @date: 2020/3/27 15:16
 * @description: 动态数据源自定义事务管理器
 * @version: JDK 1.8
 */
@Configuration(value = "ddstm")
public class DynamicDataSourceTransactionManager extends DataSourceTransactionManager
{
    public DynamicDataSourceTransactionManager(@Qualifier(value = "dynamicDataSource") DynamicDataSource dynamicDataSource)
    {
        super();
        this.setDataSource(dynamicDataSource);
    }

    @Override
    protected void doBegin(Object transaction, TransactionDefinition definition)
    {
        JdbcTransactionObjectSupport jdbcTransactionObjectSupport = (JdbcTransactionObjectSupport) transaction;
        ((JdbcTransactionObjectSupport) transaction).setConnectionHolder(null);
        super.doBegin(transaction, definition);
    }
}
