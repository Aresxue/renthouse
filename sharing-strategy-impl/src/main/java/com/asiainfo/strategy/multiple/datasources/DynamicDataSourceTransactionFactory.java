package com.asiainfo.strategy.multiple.datasources;

import org.apache.ibatis.session.TransactionIsolationLevel;
import org.apache.ibatis.transaction.Transaction;
import org.mybatis.spring.transaction.SpringManagedTransactionFactory;

import javax.sql.DataSource;

/**
 * @author: Ares
 * @date: 2020/3/31 16:44
 * @description: 动态数据源自定义事务生产工厂
 * @version: JDK 1.8
 */
public class DynamicDataSourceTransactionFactory extends SpringManagedTransactionFactory
{
    /**
     * @author: Ares
     * @description: 重写事务生成方法, 返回自定义事务
     * @date: 2020/3/31 16:45
     * @param: [dataSource, level, autoCommit] 请求参数
     * @return: org.apache.ibatis.transaction.Transaction 响应参数
     */
    @Override
    public Transaction newTransaction(DataSource dataSource, TransactionIsolationLevel level, boolean autoCommit) {
        return new DynamicDataSourceTransaction(dataSource);
    }
}
