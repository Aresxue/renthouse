package com.asiainfo.frame.annotations;

import com.asiainfo.frame.remote.invoke.ProviderAnnotationBeanPostProcessor;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.lang.NonNull;
import org.springframework.util.ClassUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author: Ares
 * @date: 2020/4/28 13:30
 * @description: 生成提供者注册类
 * @version: JDK 1.8
 */
public class AresComponentScanRegistrar implements ImportBeanDefinitionRegistrar
{
    @Override
    public void registerBeanDefinitions(@NonNull AnnotationMetadata importingClassMetadata, @NonNull BeanDefinitionRegistry registry)
    {
        Set<String> packagesToScan = getPackagesToScan(importingClassMetadata);
        registerProviderAnnotationBeanPostProcessor(packagesToScan, registry);
    }

    private void registerProviderAnnotationBeanPostProcessor(Set<String> packagesToScan, BeanDefinitionRegistry registry)
    {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(ProviderAnnotationBeanPostProcessor.class);
        builder.addConstructorArgValue(packagesToScan);
        builder.setRole(2);
        AbstractBeanDefinition beanDefinition = builder.getBeanDefinition();
        BeanDefinitionReaderUtils.registerWithGeneratedName(beanDefinition, registry);
    }

    /**
     * @author: Ares
     * @description: 提取注解内配置的包名
     * @date: 2020/4/29 16:04
     * @param: [metadata] 请求参数
     * @return: java.util.Set<java.lang.String> 响应参数
     */
    private Set<String> getPackagesToScan(AnnotationMetadata metadata)
    {
        AnnotationAttributes attributes = AnnotationAttributes.fromMap(metadata.getAnnotationAttributes(AresComponentScan.class.getName()));
        String[] basePackages = attributes.getStringArray("basePackages");
        Class<?>[] basePackageClasses = attributes.getClassArray("basePackageClasses");
        String[] value = attributes.getStringArray("value");

        Set<String> packagesToScan = new LinkedHashSet<>(Arrays.asList(value));
        packagesToScan.addAll(Arrays.asList(basePackages));
        Arrays.stream(basePackageClasses).forEach(basePackageClass -> packagesToScan.add(ClassUtils.getPackageName(basePackageClass)));

        return packagesToScan.isEmpty() ? Collections.singleton(ClassUtils.getPackageName(metadata.getClassName())) : packagesToScan;
    }
}