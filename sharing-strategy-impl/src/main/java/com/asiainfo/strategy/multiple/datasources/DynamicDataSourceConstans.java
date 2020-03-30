package com.asiainfo.strategy.multiple.datasources;

/**
 * @author: Ares
 * @date: 2020/3/19 13:07
 * @description: 动态数据源常量
 * @version: JDK 1.8
 */
public class DynamicDataSourceConstans
{
    /**
     * 自定义数据源配置信息前缀
     */
    public static final String CUSTOM_DATASOURCE_PREFIX = "custom.datasource.";
    /**
     * 默认数据源标识在自定义数据源集合中的标识
     */
    public static final String DEFAULT_TARGET_DATASOURCE = "defaultTargetDataSource";
    /**
     * 自定义数据源配置信息分隔符
     */
    public static final String CUSTOM_DATASOURCE_DELIMITER = ",";
    /**
     * 类名和方法名的连接符
     */
    public static final String CLASS_AND_METHOD_CONNECTOR = "#";
    /**
     * 事务启动标识
     */
    public static final String DYNAMIC_TRANSACTION_MANAGER_EXIST = "dynamicTransactionManagerExist";
    /**
     * 事务只读标识
     */
    public static final String DYNAMIC_TRANSACTION_MANAGER_ROLLBACKONLY = "dynamicTransactionManagerRollbackOnly";

}
