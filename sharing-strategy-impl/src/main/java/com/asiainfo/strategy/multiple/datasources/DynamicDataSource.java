package com.asiainfo.strategy.multiple.datasources;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import javax.sql.DataSource;
import java.util.Map;

/**
 * @author: Ares
 * @date: 2020/3/19 11:39
 * @description: 动态数据源
 * @version: JDK 1.8
 */
public class DynamicDataSource extends AbstractRoutingDataSource
{
    /**
     * @author: Ares
     * @description: 配置DataSource, defaultTargetDataSource为主数据库
     * targetDataSources为自定义数据源集合
     * @date: 2020/3/19 11:42
     * @param: [defaultTargetDataSource, targetDataSources] 请求参数
     * @return: 响应参数
     */
    public DynamicDataSource(DataSource defaultTargetDataSource, Map<Object, Object> targetDataSources)
    {
        super.setDefaultTargetDataSource(defaultTargetDataSource);
        super.setTargetDataSources(targetDataSources);
        super.afterPropertiesSet();
    }

    /**
     * @author: Ares
     * @description: 根据数据库标识进行数据源路由
     * @date: 2020/3/19 11:42
     * @param: [] 请求参数
     * @return: java.lang.Object 响应参数
     */
    @Override
    public Object determineCurrentLookupKey()
    {
        return DynamicDataSourceUtil.getDataSourceId();
    }
}
