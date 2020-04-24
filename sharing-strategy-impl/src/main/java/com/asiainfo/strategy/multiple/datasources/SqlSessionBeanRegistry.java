package com.asiainfo.strategy.multiple.datasources;

import com.alibaba.druid.pool.DruidDataSource;
import com.asiainfo.frame.utils.PropertiesUtil;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.annotation.AnnotatedGenericBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.annotation.AnnotationScopeMetadataResolver;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopeMetadata;
import org.springframework.context.annotation.ScopeMetadataResolver;
import org.springframework.lang.NonNull;

import java.util.Arrays;
import java.util.Properties;

import static com.asiainfo.strategy.multiple.datasources.MultipleDataSourceConstants.CUSTOM_DATASOURCE_DELIMITER;
import static com.asiainfo.strategy.multiple.datasources.MultipleDataSourceConstants.CUSTOM_DATASOURCE_IDS;
import static com.asiainfo.strategy.multiple.datasources.MultipleDataSourceConstants.CUSTOM_DATASOURCE_PREFIX;
import static com.asiainfo.strategy.multiple.datasources.MultipleDataSourceConstants.CUSTOM_DATASOURCE_PROPERTIES;
import static com.asiainfo.strategy.multiple.datasources.MultipleDataSourceConstants.SQL_SESSION_FACTORY_BEAN_NAME;

/**
 * @author: Ares
 * @date: 2020/4/23 20:25
 * @description: 注册sqlSessionFactory和sqlSessionTemplate, 提供给额外数据源使用
 * @version: JDK 1.8
 */
@Configuration
public class SqlSessionBeanRegistry implements BeanDefinitionRegistryPostProcessor
{
    private static final Logger LOGGER = LoggerFactory.getLogger(SqlSessionBeanRegistry.class);

    private final ScopeMetadataResolver scopeMetadataResolver = new AnnotationScopeMetadataResolver();

    @Override
    public void postProcessBeanDefinitionRegistry(@NonNull BeanDefinitionRegistry beanDefinitionRegistry) throws BeansException
    {
        Object datasourceIds = PropertiesUtil.getPropertyByPrefix(CUSTOM_DATASOURCE_PROPERTIES, CUSTOM_DATASOURCE_IDS);
        if (null != datasourceIds)
        {
            Arrays.stream(String.valueOf(datasourceIds)
                    .split(CUSTOM_DATASOURCE_DELIMITER))
                    .forEach(datasourceId -> {
                AnnotatedBeanDefinition annotatedBeanDefinition = new AnnotatedGenericBeanDefinition(SqlSessionFactoryBean.class);

                ScopeMetadata scopeMetadata = this.scopeMetadataResolver.resolveScopeMetadata(annotatedBeanDefinition);
                annotatedBeanDefinition.setScope(scopeMetadata.getScopeName());
                // 不参与自动注入(默认也是这里再次显示声明), 不然默认数据源byType装配时会发现多导致注入失败
                annotatedBeanDefinition.setAutowireCandidate(false);

                DruidDataSource druidDataSource = new DruidDataSource();
                Properties properties = PropertiesUtil.getPropertiesByPrefix(CUSTOM_DATASOURCE_PROPERTIES, CUSTOM_DATASOURCE_PREFIX + datasourceId);
                druidDataSource.configFromPropety(properties);
                annotatedBeanDefinition.getPropertyValues().addPropertyValue("dataSource", druidDataSource);

                BeanDefinitionHolder beanDefinitionHolder = new BeanDefinitionHolder(annotatedBeanDefinition, SQL_SESSION_FACTORY_BEAN_NAME + datasourceId);
                BeanDefinitionReaderUtils.registerBeanDefinition(beanDefinitionHolder, beanDefinitionRegistry);
            });
        }
    }

    @Override
    public void postProcessBeanFactory(@NonNull ConfigurableListableBeanFactory configurableListableBeanFactory) throws BeansException
    {

    }
}
