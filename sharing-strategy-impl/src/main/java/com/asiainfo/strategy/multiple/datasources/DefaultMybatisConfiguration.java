package com.asiainfo.strategy.multiple.datasources;

import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceBuilder;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import javax.sql.DataSource;

/**
 * @author: Ares
 * @date: 2020/3/19 12:16
 * @description: 默认mybatis配置类
 * @version: JDK 1.8
 */
@Configuration
public class DefaultMybatisConfiguration
{
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

    @Bean
    public SqlSessionFactory sqlSessionFactory() throws Exception
    {
        SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
        factoryBean.setDataSource(initDefaultTargetDataSource());
        // 指定mapper xml目录
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        factoryBean.setMapperLocations(resolver.getResources("classpath:mapper/*.xml"));
        return factoryBean.getObject();
    }

    @Bean
    public SqlSessionTemplate sqlSessionTemplate() throws Exception
    {
        return new SqlSessionTemplate(sqlSessionFactory());
    }
}
