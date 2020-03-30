package com.asiainfo.strategy.multiple.datasources;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.ConnectionHolder;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.lang.NonNull;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.ResourceTransactionManager;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author: Ares
 * @date: 2020/3/27 15:16
 * @description: 动态数据源自定义事务管理器
 * 必须搭配TargetDataSource注解使用
 * 该事务管理器只支持PROPAGATION_REQUIRED传播级别
 * 不建议使用
 * @version: JDK 1.8
 */
@Deprecated
@Configuration(value = "dynamicDataSourceTransactionManagerDemo")
public class DynamicDataSourceTransactionManagerDemo extends AbstractPlatformTransactionManager implements ResourceTransactionManager, InitializingBean
{
    private static final Logger LOGGER = LoggerFactory.getLogger(DynamicDataSourceTransactionManagerDemo.class);

    @Autowired
    @Qualifier(value = "dynamicDataSource")
    private DynamicDataSource dynamicDataSource;

    /**
     * 动态数据眼事务管理标识 线程安全值
     */
    private static final ThreadLocal<Map<String, Boolean>> DYNAMIC_DATASOURCE_TRANSACTION_MANAGER = ThreadLocal.withInitial(() -> {
        Map<String, Boolean> dynamicTransactionMap = new HashMap<>(2);
        dynamicTransactionMap.put("dynamicTransactionManagerExist", Boolean.FALSE);
        dynamicTransactionMap.put("dynamicTransactionManagerRollbackOnly", Boolean.FALSE);
        return dynamicTransactionMap;
    });

    @Override
    public void afterPropertiesSet()
    {
        LOGGER.debug("动态数据源自定义事务管理器准备完毕");
    }

    /**
     * @author: Ares
     * @description: 在整个事务处理都用到的连接
     * @date: 2020/3/27 17:10
     * @param: [] 请求参数
     * @return: java.lang.Object 响应参数
     */
    @Override
    @NonNull
    protected Object doGetTransaction() throws TransactionException
    {
        return new HashMap<String, Connection>();
    }

    /**
     * @author: Ares
     * @description: 判断是否已存在事务
     * @date: 2020/3/27 17:17
     * @param: [transaction] 事务
     * @return: boolean 响应参数
     */
    @Override
    protected boolean isExistingTransaction(Object transaction)
    {
        return DYNAMIC_DATASOURCE_TRANSACTION_MANAGER.get().getOrDefault("dynamicTransactionManagerExist", false);
    }

    /**
     * @author: Ares
     * @description: 设置准备回滚
     * @date: 2020/3/27 18:39
     * @param: [status] 请求参数
     * @return: void 响应参数
     */
    @Override
    protected void doSetRollbackOnly(DefaultTransactionStatus status)
    {
        // 标记事务管理器在线程内已准备要回滚
        DYNAMIC_DATASOURCE_TRANSACTION_MANAGER.get().put("dynamicTransactionManagerRollbackOnly", Boolean.TRUE);
    }

    @Override
    protected void doBegin(@NonNull Object transaction, @NonNull TransactionDefinition transactionDefinition) throws TransactionException
    {
        LOGGER.info("动态数据源自定义分布式事务开始, 当前线程 :{}", Thread.currentThread().getId());

        // 默认数据源链接排在最后
        Map<String, Connection> connectionMap = (Map<String, Connection>) transaction;

        // 遍历系统中的所有数据源, 打开链接
        DynamicDataSourceUtil.getDataSourceMap().forEach((datasourceId, datasource) -> {
            Connection connection;
            try
            {
                ConnectionHolder conHolder = (ConnectionHolder) TransactionSynchronizationManager.getResource(datasource);
                if (null == conHolder)
                {
                    connection = datasource.getConnection();
                    connection.setAutoCommit(false);
                    // 缓存链接
                    TransactionSynchronizationManager.bindResource(datasource, new ConnectionHolder(connection));
                    if (DynamicDataSourceUtil.getDataSourceId().equals(datasourceId))
                    {
                        TransactionSynchronizationManager.bindResource(dynamicDataSource, new ConnectionHolder(connection));
                    }
                }
                else
                {
                    connection = conHolder.getConnection();
                }

                connectionMap.put(datasourceId, connection);
                LOGGER.debug("数据源: {}打开连接成功", datasourceId);
            } catch (Throwable ex)
            {
                doCleanupAfterCompletion(connectionMap);
                throw new CannotCreateTransactionException("数据源: " + datasourceId + "打开连接错误", ex);
            }
        });


        // 标记事务管理器已经在线程内启动
        DYNAMIC_DATASOURCE_TRANSACTION_MANAGER.get().put("dynamicTransactionManagerExist", Boolean.TRUE);
    }


    /**
     * @author: Ares
     * @description: 提交事务
     * @date: 2020/3/27 18:05
     * @param: [defaultTransactionStatus] 请求参数
     * @return: void 响应参数
     */
    @Override
    protected void doCommit(@NonNull DefaultTransactionStatus defaultTransactionStatus) throws TransactionException
    {
        Map<String, Connection> connectionMap = (Map<String, Connection>) defaultTransactionStatus.getTransaction();
        connectionMap.forEach((datasourceId, connection) -> {
            try
            {
                connection.commit();
                LOGGER.debug("数据源: {}提交事务成功", datasourceId);
            } catch (SQLException ex)
            {
                doCleanupAfterCompletion(connectionMap);
                throw new TransactionSystemException("数据源: " + datasourceId + "提交事务失败", ex);
            }
        });

        LOGGER.info("动态数据源自定义分布式事务结束, 当前线程: {}", Thread.currentThread().getId());
    }


    /**
     * @author: Ares
     * @description: 回滚事务
     * @date: 2020/3/27 18:08
     * @param: [defaultTransactionStatus] 请求参数
     * @return: void 响应参数
     */
    @Override
    protected void doRollback(@NonNull DefaultTransactionStatus defaultTransactionStatus) throws TransactionException
    {
        Map<String, Connection> connectionMap = (Map<String, Connection>) defaultTransactionStatus.getTransaction();
        connectionMap.forEach((datasourceId, connection) -> {
            try
            {
                connection.rollback();
                LOGGER.debug("数据源: {}回滚事务成功", datasourceId);
            } catch (SQLException ex)
            {
                doCleanupAfterCompletion(connectionMap);
                throw new TransactionSystemException("数据源: " + datasourceId + "回滚事务失败", ex);
            }
        });

        LOGGER.info("动态数据源自定义分布式事务回滚, 当前线程: {} ", Thread.currentThread().getId());
    }

    @Override
    @NonNull
    public Object getResourceFactory()
    {
        return dynamicDataSource;
    }

    /**
     * @author: Ares
     * @description: 回收链接
     * @date: 2020/3/27 18:08
     * @param: [transaction] 请求参数
     * @return: void 响应参数
     */
    @Override
    protected void doCleanupAfterCompletion(Object transaction)
    {
        Map<String, Connection> connectionMap = (Map<String, Connection>) transaction;
        // 关闭额外数据源链接
        connectionMap.forEach((datasourceId, connection) -> {
            DataSource dataSource = DynamicDataSourceUtil.getDataSource(datasourceId);
            TransactionSynchronizationManager.unbindResource(dataSource);
            DataSourceUtils.releaseConnection(connection, dataSource);
            LOGGER.debug("额外数据源: {}关闭链接成功", datasourceId);
        });

        // 释放本地资源
        if (TransactionSynchronizationManager.hasResource(dynamicDataSource))
        {
            TransactionSynchronizationManager.unbindResource(dynamicDataSource);
        }

        DYNAMIC_DATASOURCE_TRANSACTION_MANAGER.get().remove("dynamicTransactionManagerExist");
        DYNAMIC_DATASOURCE_TRANSACTION_MANAGER.get().remove("dynamicTransactionManagerRollbackOnly");
        DYNAMIC_DATASOURCE_TRANSACTION_MANAGER.remove();

        LOGGER.info("动态数据源自定义分布式事务释放, 当前线程: {}", Thread.currentThread().getId());
    }
}

