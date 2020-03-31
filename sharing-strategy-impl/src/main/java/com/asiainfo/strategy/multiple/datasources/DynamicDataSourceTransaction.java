package com.asiainfo.strategy.multiple.datasources;

import org.apache.ibatis.transaction.Transaction;
import org.mybatis.logging.Logger;
import org.mybatis.logging.LoggerFactory;
import org.springframework.jdbc.datasource.DataSourceUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static com.asiainfo.strategy.multiple.datasources.DynamicDataSourceConstans.DEFAULT_TARGET_DATASOURCE;

/**
 * @author: Ares
 * @date: 2020/3/30 18:49
 * @description: 动态数据源自定义事务
 * 从SpringManagedTransaction改造而来
 * @version: JDK 1.8
 */
public class DynamicDataSourceTransaction implements Transaction
{
    private static final Logger LOGGER = LoggerFactory.getLogger(DynamicDataSourceTransaction.class);

    private final DataSource dataSource;
    /**
     * 默认数据库链接
     */
    private Connection defaultConnection;
    private boolean isConnectionTransactional;
    private Map<String, Boolean> autoCommitMap = new HashMap<>(8);

    /**
     * 自定义数据库链接集合
     */
    private ConcurrentMap<String, Connection> connectionMap = new ConcurrentHashMap<>();

    public DynamicDataSourceTransaction(DataSource dataSource)
    {
        this.dataSource = dataSource;
    }


    @Override
    public Connection getConnection() throws SQLException
    {
        String dataSourceId = DynamicDataSourceUtil.getDataSourceId();
        if (DEFAULT_TARGET_DATASOURCE.equals(dataSourceId))
        {
            if (this.defaultConnection == null)
            {
                this.openConnection();
            }

            return this.defaultConnection;
        }
        // 不是默认数据源
        if (connectionMap.containsKey(dataSourceId))
        {
            return connectionMap.get(dataSourceId);
        }
        // 链接不存在时获取链接, 这里是关键, 使用原生的getConnection,
        // 会执行AbstractRoutingDataSource的逻辑获取想要的链接
        Connection connection = dataSource.getConnection();
        this.autoCommitMap.put(dataSourceId, connection.getAutoCommit());
        connectionMap.put(dataSourceId, connection);

        return connection;
    }

    private void openConnection() throws SQLException
    {
        this.defaultConnection = DataSourceUtils.getConnection(this.dataSource);
        this.autoCommitMap.put(DEFAULT_TARGET_DATASOURCE, this.defaultConnection.getAutoCommit());
        this.isConnectionTransactional = DataSourceUtils.isConnectionTransactional(this.defaultConnection, this.dataSource);
        LOGGER.debug(() -> "JDBC Connection [" + this.defaultConnection + "] will" + (this.isConnectionTransactional ? " " : " not ") + "be managed by Spring");
    }

    @Override
    public void commit() throws SQLException
    {
        // 先提交自定义数据源的事务, 保证出错时默认数据库不会受到影响
        for (Map.Entry<String, Connection> entry : connectionMap.entrySet())
        {
            if (entry.getValue() != null && !autoCommitMap.get(entry.getKey()))
            {
                LOGGER.debug(() -> "Committing JDBC Connection [" + entry.getValue() + "]");
                entry.getValue().commit();
            }
        }

        if (this.defaultConnection != null && !this.isConnectionTransactional && !this.autoCommitMap.get(DEFAULT_TARGET_DATASOURCE))
        {
            LOGGER.debug(() -> "Committing JDBC Connection [" + this.defaultConnection + "]");
            this.defaultConnection.commit();
        }
    }

    @Override
    public void rollback() throws SQLException
    {
        if (this.defaultConnection != null && !this.isConnectionTransactional && !this.autoCommitMap.get(DEFAULT_TARGET_DATASOURCE))
        {
            LOGGER.debug(() -> "Rolling back JDBC Connection [" + this.defaultConnection + "]");
            this.defaultConnection.rollback();
        }

        // 后回滚自定义数据源的事务, 保证出错时默认数据库不会受到影响
        for (Map.Entry<String, Connection> entry : connectionMap.entrySet())
        {
            if (entry.getValue() != null && !autoCommitMap.get(entry.getKey()))
            {
                LOGGER.debug(() -> "Rolling back JDBC Connection [" + entry.getValue() + "]");
                entry.getValue().rollback();
            }
        }

    }

    @Override
    public void close()
    {
        DataSourceUtils.releaseConnection(this.defaultConnection, this.dataSource);

        for (Connection connection : connectionMap.values())
        {
            DataSourceUtils.releaseConnection(connection, this.dataSource);
        }
    }

    @Override
    public Integer getTimeout()
    {
        return null;
    }
}
