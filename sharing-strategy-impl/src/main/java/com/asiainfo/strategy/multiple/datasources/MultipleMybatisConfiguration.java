package com.asiainfo.strategy.multiple.datasources;

import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.mapper.MapperScannerConfigurer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Configuration
public class MultipleMybatisConfiguration implements  ImportBeanDefinitionRegistrar
{
    private static final Logger LOGGER = LoggerFactory.getLogger(MultipleMybatisConfiguration.class);

    private void registerMybatisConfiguration(BeanDefinitionRegistry registry, String datasourceId)
    {
        List<String> packages = new ArrayList<>();
        packages.add("com.asiainfo.strategy");
        if (LOGGER.isDebugEnabled())
        {
            LOGGER.debug("Searching for mappers annotated with @Mapper");
            packages.forEach((pkg) -> {
                MultipleMybatisConfiguration.LOGGER.debug("Using auto-configuration base package '{}'", pkg);
            });
        }

        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(MapperScannerConfigurer.class);
        builder.addPropertyValue("processPropertyPlaceHolders", true);
        builder.addPropertyValue("annotationClass", Mapper.class);
        builder.addPropertyValue("basePackage", StringUtils.collectionToCommaDelimitedString(packages));
        BeanWrapper beanWrapper = new BeanWrapperImpl(MapperScannerConfigurer.class);
        Stream.of(beanWrapper.getPropertyDescriptors())
                .filter((x) -> "lazyInitialization".equals(x.getName()))
                .findAny()
                .ifPresent((x) -> {
                    builder.addPropertyValue("lazyInitialization", "${mybatis.lazy-initialization:false}");
                });

        builder.addPropertyValue("sqlSessionFactoryBeanName", "sqlSessionFactory"+datasourceId);
        builder.addPropertyValue("sqlSessionTemplateBeanName", "sqlSessionTemplate"+datasourceId);
        registry.registerBeanDefinition(MapperScannerConfigurer.class.getName(), builder.getBeanDefinition());
    }
}
