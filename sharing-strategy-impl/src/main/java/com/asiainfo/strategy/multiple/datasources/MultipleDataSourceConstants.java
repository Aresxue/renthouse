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
     * @author: Ares
     * @description: 自定义数据源*Mapper.xml地址配置项格式
     * @date: 2020/4/25 18:47
     * @param:  请求参数
     * @return:  响应参数
     */
    public static final String CUSTOM_DATASOURCE_MYBATIS_MAPPER_LOCATIONS = "%s%s.mybatis.mapper.locations";
    /**
     * @author: Ares
     * @description: 自定义数据源mybatis懒加载配置
     * @date: 2020/4/25 18:56
     * @param:  请求参数
     * @return:  响应参数
     */
    public static final String CUSTOM_DATASOURCE_MYBATIS_LAZY_INITIALIZATION = "${%s%s.mybatis.lazy-initialization:false}";
    /**
     * 自定义数据源mybatis扫描包目录配置
     */
    public static final String CUSTOM_DATASOURCE_MYBATIS_BASE_PACKAGES = "%s%s.mybatis.basePackages";
    /**
     * @author: Ares
     * @description: 自定义数据源默认*Mapper.xml地址格式, 字符串参数为数据源标识
     * @date: 2020/4/25 18:45
     * @param:  请求参数
     * @return:  响应参数
     */
    public static final String DEFAULT_MAPPER_XML_CLASSPATH = "classpath:mapper/%s/*.xml";
    /**
     * 自定义数据源配置信息分隔符
     */
    public static final String CUSTOM_DATASOURCE_DELIMITER = ",";

    public static final String SQL_SESSION_FACTORY_BEAN_NAME = "sqlSessionFactory";
    public static final String SQL_SESSION_TEMPLATE_BEAN_NAME = "sqlSessionTemplate";

}
