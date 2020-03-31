package com.asiainfo.strategy.multiple.datasources;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

/**
 * @author: Ares
 * @date: 2020/3/31 16:53
 * @description: 动态数据源自定义SqlSessionFactory
 * @version: JDK 1.8
 */
@Configuration
public class SqlSessionFactoryConfiguration
{
    private static final Logger LOGGER = LoggerFactory.getLogger(SqlSessionFactoryConfiguration.class);


    @Autowired
    @Qualifier(value = "dynamicDataSource")
    private DynamicDataSource dynamicDataSource;

    @Bean(name = "sqlSessionFactory")
    public SqlSessionFactory sqlSessionFactorys() throws Exception
    {
        LOGGER.info("--------------------  sqlSessionFactory init ---------------------");
        try
        {
            SqlSessionFactoryBean sessionFactoryBean = new SqlSessionFactoryBean();
            sessionFactoryBean.setDataSource(dynamicDataSource);
            sessionFactoryBean.setTransactionFactory(new DynamicDataSourceTransactionFactory());
            return sessionFactoryBean.getObject();
        } catch (IOException e)
        {
            LOGGER.error("mybatis resolver mapper*xml is error", e);
        } catch (Exception e)
        {
            LOGGER.error("mybatis sqlSessionFactoryBean create error", e);
        }
        return null;
    }
}
