package com.asiainfo.strategy.multiple.datasources;

/**
 * @author: Ares
 * @date: 2020/3/19 13:07
 * @description: 多数据源常量
 * @version: JDK 1.8
 */
public class MultipleDataSourceConstants
{
    /**
     * 自定义数据源标识集合
     */
    public static final String CUSTOM_DATASOURCE_IDS = "custom.datasource.ids";
    /**
     * 自定义数据源配置信息前缀
     */
    public static final String CUSTOM_DATASOURCE_PREFIX = "custom.datasource.";
    /**
     * 自定义数据源配置文件名
     */
    public static final String CUSTOM_DATASOURCE_PROPERTIES = "custom-datasource.properties";
    /**
     * 自定义数据源配置信息分隔符
     */
    public static final String CUSTOM_DATASOURCE_DELIMITER = ",";

    public static final String SQL_SESSION_FACTORY_BEAN_NAME = "sqlSessionFactory";
    public static final String SQL_SESSION_TEMPLATE_BEAN_NAME = "sqlSessionTemplate";

}
