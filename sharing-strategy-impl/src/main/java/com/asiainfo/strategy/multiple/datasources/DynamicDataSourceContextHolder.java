package com.asiainfo.strategy.multiple.datasources;

import java.util.HashSet;
import java.util.Set;

/**
 * @author: Ares
 * @date: 2020/3/19 11:40
 * @description: 动态数据源线程安全类
 * @version: JDK 1.8
 */
public class DynamicDataSourceContextHolder
{
    /**
     * 数据源标识 线程安全值
     */
    private static final ThreadLocal<String> CONTEXT_HOLDER = new ThreadLocal<>();
    /**
     * 数据源标识集合, 可用于遍历数据源, 执行数据源获取和判断操作
     * 主数据源标识为defaultTargetDataSource
     */
    public static Set<String> dataSourceIds = new HashSet<>(8);

    public static void setDataSourceId(String dataSourceName)
    {
        CONTEXT_HOLDER.set(dataSourceName);
    }

    public static String getDataSourceId()
    {
        return CONTEXT_HOLDER.get();
    }

    public static void clearDataSourceId()
    {
        CONTEXT_HOLDER.remove();
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
        return dataSourceIds.contains(dataSourceId);
    }
}
