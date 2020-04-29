package com.asiainfo.frame.remote.invoke;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.AnnotationConfigUtils;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.lang.NonNull;

import java.util.Set;

/**
 * @author: Ares
 * @date: 2020/4/28 14:07
 * @description: 类定义解析扫描
 * @version: JDK 1.8
 */
public class AresClassPathBeanDefinitionScanner extends ClassPathBeanDefinitionScanner
{
    public AresClassPathBeanDefinitionScanner(BeanDefinitionRegistry registry, boolean useDefaultFilters, Environment environment, ResourceLoader resourceLoader)
    {
        super(registry, useDefaultFilters);
        this.setEnvironment(environment);
        this.setResourceLoader(resourceLoader);
        AnnotationConfigUtils.registerAnnotationConfigProcessors(registry);
    }

    public AresClassPathBeanDefinitionScanner(BeanDefinitionRegistry registry, Environment environment, ResourceLoader resourceLoader)
    {
        this(registry, false, environment, resourceLoader);
    }

    @Override
    @NonNull
    public Set<BeanDefinitionHolder> doScan(@NonNull String... basePackages)
    {
        return super.doScan(basePackages);
    }

    @Override
    public boolean checkCandidate(@NonNull String beanName, @NonNull BeanDefinition beanDefinition) throws IllegalStateException
    {
        return super.checkCandidate(beanName, beanDefinition);
    }
}
