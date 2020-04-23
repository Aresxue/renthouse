package com.asiainfo.strategy.multiple.datasources;

import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.mapper.MapperScannerConfigurer;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.lang.NonNull;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Stream;

/**
 * @author: Ares
 * @date: 2020/4/23 17:41
 * @description: mybatis多配置注册
 * @version: JDK 1.8
 */
@Configuration
public class MultipleMybatisConfigurationRegistry implements BeanFactoryAware, ImportBeanDefinitionRegistrar
{
    private BeanFactory beanFactory;

    @Override
    public void setBeanFactory(@NonNull BeanFactory beanFactory) throws BeansException
    {
        this.beanFactory = beanFactory;
    }

    @Override
    public void registerBeanDefinitions(@NonNull AnnotationMetadata importingClassMetadata, @NonNull BeanDefinitionRegistry registry, @NonNull BeanNameGenerator importBeanNameGenerator)
    {
        registryMybatisConfiguration(registry, null);
    }


    /**
     * @author: Ares
     * @description: 根据数据源标识创建配置并注册到Spring
     * @date: 2020/4/23 18:36
     * @param: [registry, datasourceId] 请求参数
     * @return: void 响应参数
     */
    private void registryMybatisConfiguration(@NonNull BeanDefinitionRegistry registry, String datasourceId)
    {
        List<String> packages = AutoConfigurationPackages.get(this.beanFactory);

        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(MapperScannerConfigurer.class);
        builder.addPropertyValue("processPropertyPlaceHolders", true);
        builder.addPropertyValue("annotationClass", Mapper.class);
        builder.addPropertyValue("basePackage", StringUtils.collectionToCommaDelimitedString(packages));
        BeanWrapper beanWrapper = new BeanWrapperImpl(MapperScannerConfigurer.class);
        Stream.of(beanWrapper.getPropertyDescriptors()).filter((x) -> "lazyInitialization".equals(x.getName())).findAny().ifPresent((x) -> {
            builder.addPropertyValue("lazyInitialization", "${mybatis.lazy-initialization:false}");
        });

        if (null != datasourceId)
        {
        builder.addPropertyValue("sqlSessionFactoryBeanName", "sqlSessionFactory" + datasourceId);
        builder.addPropertyValue("sqlSessionTemplateBeanName", "sqlSessionTemplate" + datasourceId);
        }

        registry.registerBeanDefinition(MapperScannerConfigurer.class.getName(), builder.getBeanDefinition());
    }

    @Override
    public void registerBeanDefinitions(@NonNull AnnotationMetadata importingClassMetadata, @NonNull BeanDefinitionRegistry registry)
    {

    }
}
