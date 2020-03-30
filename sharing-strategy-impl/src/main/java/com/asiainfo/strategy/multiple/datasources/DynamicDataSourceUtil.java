package com.asiainfo.strategy.multiple.datasources;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.datasource.ConnectionHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.sql.DataSource;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.asiainfo.strategy.multiple.datasources.DynamicDataSourceConstans.DEFAULT_TARGET_DATASOURCE;

/**
 * @author: Ares
 * @date: 2020/3/19 11:39
 * @description: 动态数据工具类
 * @version: JDK 1.8
 */
@Component
public class DynamicDataSourceUtil
{
    private static final Logger LOGGER = LoggerFactory.getLogger(DynamicDataSourceUtil.class);

    @Autowired
    @Qualifier(value = "dynamicDataSource")
    private static DynamicDataSource dynamicDataSource;

    /**
     * @author: Ares
     * @description: 根据传入的数据源标识切换数据源
     * @date: 2020/3/20 10:07
     * @param: [datasourceId]
     * 数据源标识
     * @return: void 响应参数
     */
    public static void changeDataSource(String dataSourceId)
    {
        // 调用该方法的上层类的全名
        String className = Thread.currentThread().getStackTrace()[2].getClassName();
        // 调用该方法的上层方法名称
        String invokeMethodName = Thread.currentThread().getStackTrace()[2].getMethodName();
        changeDataSource(dataSourceId, className + DynamicDataSourceConstans.CLASS_AND_METHOD_CONNECTOR + invokeMethodName);
    }

    /**
     * @author: Ares
     * @description: 根据传入的数据源标识切换数据源
     * @date: 2020/3/20 10:07
     * @param: [datasourceId, methodName]
     * 数据源标识, 方法名
     * @return: void 响应参数
     */
    public static void changeDataSource(String dataSourceId, String methodName)
    {
        // 第一次初始化的时候数据源改变标识不处理
        if (null != DynamicDataSourceUtil.getDataSourceId())
        {
            // 设置数据源是否改变
            setDatasourceChange(!dataSourceId.equals(DynamicDataSourceUtil.getDataSourceId()));
        }
        if (null == getDatasourceChange() || getDatasourceChange())
        {
            if (!DynamicDataSourceUtil.containsDataSource(dataSourceId))
            {
                LOGGER.error("数据源: {}不存在，方法: {}使用默认数据源", dataSourceId, methodName);
                DynamicDataSourceUtil.setDataSourceId(DEFAULT_TARGET_DATASOURCE);
            }
            else
            {
                LOGGER.debug("方法: {}切换至指定数据库: {}", methodName, dataSourceId);
                DynamicDataSourceUtil.setDataSourceId(dataSourceId);
            }

            ConnectionHolder connectionHolder = (ConnectionHolder) TransactionSynchronizationManager.getResource(DynamicDataSourceUtil.getDataSource(dataSourceId));
            if (null != connectionHolder)
            {
                TransactionSynchronizationManager.unbindResource(dynamicDataSource);
                TransactionSynchronizationManager.bindResource(dynamicDataSource, connectionHolder);
            }
        }

    }


    /**
     * @author: Ares
     * @description: 设置当前线程的数据源标识
     * @date: 2020/3/27 18:16
     * @param: [dataSourceId] 请求参数
     * @return: void 响应参数
     */
    public static void setDataSourceId(String dataSourceId)
    {
        DynamicDataSourceContextHolder.CONTEXT_HOLDER.set(dataSourceId);
    }

    /**
     * @author: Ares
     * @description: 获取当前线程的数据源标识
     * @date: 2020/3/27 18:15
     * @param: [] 请求参数
     * @return: java.lang.String 响应参数
     */
    public static String getDataSourceId()
    {
        return DynamicDataSourceContextHolder.CONTEXT_HOLDER.get();
    }

    /**
     * @author: Ares
     * @description: 清理当前线程的数据源标识
     * @date: 2020/3/20 10:07
     * @param: [] 请求参数
     * @return: void 响应参数
     */
    public static void clearDataSourceId()
    {
        DynamicDataSourceContextHolder.CONTEXT_HOLDER.remove();
    }


    /**
     * @author: Ares
     * @description: 获取数据源集合
     * @date: 2020/3/27 18:11
     * @param: [] 请求参数
     * @return: java.util.Map<java.lang.String, javax.sql.DataSource> 响应参数
     */
    public static Map<String, DataSource> getDataSourceMap()
    {
        return DynamicDataSourceContextHolder.DATASOURCE_MAP;
    }

    /**
     * @author: Ares
     * @description: 根据数据源标识获取数据源
     * @date: 2020/3/27 18:23
     * @param: [datasourceId] 数据源标识
     * @return: java.util.Map<java.lang.String, javax.sql.DataSource> 响应参数
     */
    public static DataSource getDataSource(String datasourceId)
    {
        return DynamicDataSourceContextHolder.DATASOURCE_MAP.get(datasourceId);
    }

    /**
     * @author: Ares
     * @description: 添加数据源到数据源集合
     * @date: 2020/3/27 18:17
     * @param: [datasourceId, dataSource] 请求参数
     * @return: void 响应参数
     */
    public static void addDataSource(String datasourceId, DataSource dataSource)
    {
        DynamicDataSourceContextHolder.DATASOURCE_MAP.put(datasourceId, dataSource);
    }

    /**
     * @author: Ares
     * @description: 判断执行数据源当前是否存在
     * @date: 2020/3/19 12:37
     * @param: [dataSourceId] 请求参数
     * @return: boolean 响应参数
     */
    public static boolean containsDataSource(String dataSourceId)
    {
        return DynamicDataSourceContextHolder.DATASOURCE_MAP.containsKey(dataSourceId);
    }

    /**
     * @author: Ares
     * @description: 设置数据源改变标识
     * @date: 2020/3/30 17:37
     * @param: [value] 请求参数
     * @return: void 响应参数
     */
    public static void setDatasourceChange(Boolean value)
    {
        DynamicDataSourceContextHolder.datasourceChanged.set(value);
    }

    /**
     * @author: Ares
     * @description: 获取数据源改变标识
     * @date: 2020/3/30 17:37
     * @param: [] 请求参数
     * @return: boolean 数据源改变标识
     */
    public static Boolean getDatasourceChange()
    {
        return DynamicDataSourceContextHolder.datasourceChanged.get();
    }

    /**
     * @author: Ares
     * @date: 2020/3/19 11:40
     * @description: 动态数据源线程安全类
     * @version: JDK 1.8
     */
    static class DynamicDataSourceContextHolder
    {
        /**
         * 数据源标识 线程安全值
         */
        private static final ThreadLocal<String> CONTEXT_HOLDER = new ThreadLocal<>();
        /**
         * 额外数据源集合, 可用于遍历数据源, 执行数据源获取和判断操作
         */
        private static final Map<String, DataSource> DATASOURCE_MAP = new LinkedHashMap<>(8);
        /**
         * 数据源改变标识
         */
        private static volatile ThreadLocal<Boolean> datasourceChanged = new ThreadLocal<>();
    }

    @Autowired
    public void setDynamicDataSource(DynamicDataSource dynamicDataSource)
    {
        DynamicDataSourceUtil.dynamicDataSource = dynamicDataSource;
    }

}
