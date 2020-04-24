package com.asiainfo.strategy.multiple.datasources;

import com.alibaba.druid.pool.DruidDataSource;
import com.asiainfo.frame.utils.SpringUtil;
import com.asiainfo.frame.utils.StringUtil;
import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.mapper.MapperScannerConfigurer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.annotation.AnnotatedGenericBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.AnnotationScopeMetadataResolver;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopeMetadata;
import org.springframework.context.annotation.ScopeMetadataResolver;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.lang.NonNull;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Stream;

import static com.asiainfo.strategy.multiple.datasources.MultipleDataSourceConstants.CUSTOM_DATASOURCE_DELIMITER;
import static com.asiainfo.strategy.multiple.datasources.MultipleDataSourceConstants.CUSTOM_DATASOURCE_IDS;
import static com.asiainfo.strategy.multiple.datasources.MultipleDataSourceConstants.CUSTOM_DATASOURCE_PREFIX;
import static com.asiainfo.strategy.multiple.datasources.MultipleDataSourceConstants.SQL_SESSION_FACTORY_BEAN_NAME;
import static com.asiainfo.strategy.multiple.datasources.MultipleDataSourceConstants.SQL_SESSION_TEMPLATE_BEAN_NAME;

/**
 * @author: Ares
 * @date: 2020/4/23 20:25
 * @description: 注册sqlSessionFactory和sqlSessionTemplate, 提供给额外数据源使用
 * @version: JDK 1.8
 */
@Configuration
public class MybatisConfigurationRegistry implements BeanDefinitionRegistryPostProcessor, ApplicationContextAware
{
    private static final Logger LOGGER = LoggerFactory.getLogger(MybatisConfigurationRegistry.class);

    private final ScopeMetadataResolver scopeMetadataResolver = new AnnotationScopeMetadataResolver();

    private ApplicationContext applicationContext;

    @Override
    public void postProcessBeanDefinitionRegistry(@NonNull BeanDefinitionRegistry beanDefinitionRegistry) throws BeansException
    {
        String customDatasourceIds = applicationContext.getEnvironment().getProperty(CUSTOM_DATASOURCE_IDS);
        if (null != customDatasourceIds)
        {
            Arrays.stream(customDatasourceIds.split(CUSTOM_DATASOURCE_DELIMITER)).forEach(datasourceId -> {
                registerSqlSessionFactory(beanDefinitionRegistry, datasourceId);
                registerSqlSessionTemplate(beanDefinitionRegistry, datasourceId);
                registerMybatisConfiguration(beanDefinitionRegistry, datasourceId);
            });
        }
    }

    private void registerSqlSessionTemplate(BeanDefinitionRegistry beanDefinitionRegistry, String datasourceId)
    {
        AnnotatedBeanDefinition annotatedBeanDefinition = new AnnotatedGenericBeanDefinition(SqlSessionTemplate.class);

        ScopeMetadata scopeMetadata = this.scopeMetadataResolver.resolveScopeMetadata(annotatedBeanDefinition);
        annotatedBeanDefinition.setScope(scopeMetadata.getScopeName());
        // 不参与自动注入, 不然默认数据源byType装配时会发现多导致注入失败
        annotatedBeanDefinition.setAutowireCandidate(false);

        BeanDefinitionHolder beanDefinitionHolder = new BeanDefinitionHolder(annotatedBeanDefinition, SQL_SESSION_TEMPLATE_BEAN_NAME + datasourceId);
        // 添加构造器参数, 这里添加的是sqlSessionFactory*的引用, 不添加参数默认调用无参构造器
        beanDefinitionHolder.getBeanDefinition().getConstructorArgumentValues().addGenericArgumentValue(new RuntimeBeanReference(SQL_SESSION_FACTORY_BEAN_NAME + datasourceId));
        BeanDefinitionReaderUtils.registerBeanDefinition(beanDefinitionHolder, beanDefinitionRegistry);
    }

    private void registerSqlSessionFactory(@NonNull BeanDefinitionRegistry beanDefinitionRegistry, String datasourceId)
    {
        AnnotatedBeanDefinition annotatedBeanDefinition = new AnnotatedGenericBeanDefinition(SqlSessionFactoryBean.class);

        ScopeMetadata scopeMetadata = this.scopeMetadataResolver.resolveScopeMetadata(annotatedBeanDefinition);
        annotatedBeanDefinition.setScope(scopeMetadata.getScopeName());
        // 不参与自动注入, 不然默认数据源byType装配时会发现多导致注入失败
        annotatedBeanDefinition.setAutowireCandidate(false);

        // 指定数据源
        DruidDataSource druidDataSource = new DruidDataSource();
        Properties properties = SpringUtil.getPropertiesByPrefix(applicationContext.getEnvironment(), CUSTOM_DATASOURCE_PREFIX + datasourceId);
        // 将属性的key中的中划线-转为小驼峰式, 如druid.test-while-idle转为如druid.testWhileIdle
        properties.stringPropertyNames().forEach(propertyNames -> properties.setProperty(StringUtil.strikeToLittleCamelCase(propertyNames), properties.getProperty(propertyNames)));
        druidDataSource.configFromPropety(properties);
        annotatedBeanDefinition.getPropertyValues().addPropertyValue("dataSource", druidDataSource);

        // 指定*Mapper.xml目录
        String mapperFolder = applicationContext.getEnvironment().getProperty(CUSTOM_DATASOURCE_PREFIX + datasourceId + ".mybatis.mapper.locations");
        if (StringUtils.isEmpty(mapperFolder))
        {
            LOGGER.info("*Mapper.xml资源地址配置项为空, 根据默认地址获取*Mapper.xml资源");
            mapperFolder = "classpath:mapper/" + datasourceId + "/*.xml";
        }
        try
        {
            ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] mapperLocations = resolver.getResources(mapperFolder);
            annotatedBeanDefinition.getPropertyValues().addPropertyValue("mapperLocations", mapperLocations);
        } catch (IOException e)
        {
            LOGGER.error("获取*Mapper.xml资源失败: ", e);
        }

        BeanDefinitionHolder beanDefinitionHolder = new BeanDefinitionHolder(annotatedBeanDefinition, SQL_SESSION_FACTORY_BEAN_NAME + datasourceId);
        BeanDefinitionReaderUtils.registerBeanDefinition(beanDefinitionHolder, beanDefinitionRegistry);
    }

    private void registerMybatisConfiguration(BeanDefinitionRegistry registry, @NonNull String datasourceId)
    {
        List<String> packages = AutoConfigurationPackages.get(applicationContext.getAutowireCapableBeanFactory());
        String basePackages = applicationContext.getEnvironment().getProperty(CUSTOM_DATASOURCE_PREFIX + datasourceId + ".mybatis" + ".basePackages");
        if (!StringUtils.isEmpty(basePackages))
        {
            packages = Arrays.asList(basePackages.split(CUSTOM_DATASOURCE_DELIMITER));
        }

        if (LOGGER.isDebugEnabled())
        {
            LOGGER.debug("Searching for mappers annotated with @Mapper");
            packages.forEach((pkg) -> MybatisConfigurationRegistry.LOGGER.debug("Using auto-configuration base package '{}'", pkg));
        }

        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(MapperScannerConfigurer.class);
        builder.addPropertyValue("processPropertyPlaceHolders", true);
        builder.addPropertyValue("annotationClass", Mapper.class);
        builder.addPropertyValue("basePackage", StringUtils.collectionToCommaDelimitedString(packages));
        BeanWrapper beanWrapper = new BeanWrapperImpl(MapperScannerConfigurer.class);
        Stream.of(beanWrapper.getPropertyDescriptors()).filter((x) -> "lazyInitialization".equals(x.getName())).findAny().ifPresent((x) -> builder.addPropertyValue("lazyInitialization", "${" + CUSTOM_DATASOURCE_PREFIX + datasourceId + ".mybatis.lazy-initialization:false}"));

        builder.addPropertyValue("sqlSessionFactoryBeanName", SQL_SESSION_FACTORY_BEAN_NAME + datasourceId);
        builder.addPropertyValue("sqlSessionTemplateBeanName", SQL_SESSION_TEMPLATE_BEAN_NAME + datasourceId);


        registry.registerBeanDefinition(MapperScannerConfigurer.class.getName() + datasourceId, builder.getBeanDefinition());
    }

    @Override
    public void postProcessBeanFactory(@NonNull ConfigurableListableBeanFactory configurableListableBeanFactory) throws BeansException
    {

    }

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException
    {
        this.applicationContext = applicationContext;
    }
}
