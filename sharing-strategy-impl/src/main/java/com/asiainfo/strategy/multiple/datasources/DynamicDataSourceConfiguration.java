package com.asiainfo.strategy.multiple.datasources;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceBuilder;
import com.asiainfo.frame.utils.SpringUtil;
import com.asiainfo.frame.utils.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author: Ares
 * @date: 2020/3/19 12:16
 * @description: 动态数据源配置类
 * @version: JDK 1.8
 */
@Configuration
public class DynamicDataSourceConfiguration
{
    /**
     * 自定义数据源标识集合
     */
    @Value("${custom.datasource.ids:}")
    private String customDatasourceIds;

    @Autowired
    private Environment environment;

    /**
     * @author: Ares
     * @description: 默认数据源
     * @date: 2020/3/19 12:19
     * @param: [] 请求参数
     * @return: javax.sql.DataSource 响应参数
     */
    @Bean
    @ConfigurationProperties("spring.datasource.druid")
    public DataSource initDefaultTargetDataSource()
    {
        return DruidDataSourceBuilder.create().build();
    }


    /**
     * @author: Ares
     * @description: 初始化所有数据源
     * @date: 2020/3/19 12:20
     * @param: [] 请求参数
     * @return: com.asiainfo.account.multiple.data.sources.DynamicDataSource 响应参数
     */
    @Bean(name = "dynamicDataSource")
    @Primary
    public DynamicDataSource initDynamicDataSource()
    {
        DataSource defaultTargetDataSource = initDefaultTargetDataSource();
        Map<Object, Object> targetDataSources = new HashMap<>(8);
        // 添加默认数据源
        targetDataSources.put(DynamicDataSourceConstans.DEFAULT_TARGET_DATASOURCE, defaultTargetDataSource);
        DynamicDataSourceContextHolder.dataSourceIds.add(DynamicDataSourceConstans.DEFAULT_TARGET_DATASOURCE);
        // 添加自定义数据源
        if (StringUtil.isNotEmpty(customDatasourceIds))
        {
            Arrays.stream(customDatasourceIds.split(DynamicDataSourceConstans.CUSTOM_DATASOURCE_DELIMITER)).forEach(datasourceId -> {
                Properties properties = SpringUtil.getPropertiesByPrefix(environment, DynamicDataSourceConstans.CUSTOM_DATASOURCE_PREFIX + datasourceId);
                // 将属性的key中的中划线-转为小驼峰式, 如druid.test-while-idle转为如druid.testWhileIdle
                properties.stringPropertyNames().forEach(propertyNames -> properties.setProperty(StringUtil.strikeToLittleCamelCase(propertyNames), properties.getProperty(propertyNames)));
                DruidDataSource druidDataSource = new DruidDataSource();
                druidDataSource.configFromPropety(properties);
                targetDataSources.put(datasourceId, druidDataSource);
                DynamicDataSourceContextHolder.dataSourceIds.add(datasourceId);
            });
        }
        return new DynamicDataSource(defaultTargetDataSource, targetDataSources);
    }
}
