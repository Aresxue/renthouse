package com.asiainfo.frame.utils;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

/**
 * @author: Ares
 * @date: 2019/5/31 16:18
 * @description: Spring相关处理的工具类
 * @version: JDK 1.8
 */
@Component
public class SpringUtil implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext)
        throws BeansException {
        SpringUtil.applicationContext = applicationContext;
    }

    private static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    /**
     * @author: Ares
     * @description: 通过名称获取bean
     * @date: 2019/5/31 16:21
     * @param: [beanName] Bean的名称
     * @return: java.lang.Object Bean
     */
    public static Object getBean(String beanName) {
        return getApplicationContext().getBean(beanName);
    }

    /**
     * @author: Ares
     * @description: 通过class获取Bean
     * @date: 2019/5/31 16:22
     * @param: [clazz] class
     * @return: T Bean
     */
    public static <T> T getBean(Class<T> clazz) {
        return getApplicationContext().getBean(clazz);
    }

    /**
     * @author: Ares
     * @description: 通过class获取Bean
     * @date: 2019/5/31 16:23
     * @param: [beanName, clazz] Bean名称, class
     * @return: T Bean
     */
    public static <T> T getBean(String beanName, Class<T> clazz) {
        return getApplicationContext().getBean(beanName, clazz);
    }

    /**
     * @author: Ares
     * @description: 判断是否包含Bean
     * @date: 2019/5/31 16:26
     * @param: [beanName] Bean的名称
     * @return: boolean true为包含
     */
    public static boolean containsBean(String beanName) {
        return getApplicationContext().containsBean(beanName);
    }

    /**
     * @author: Ares
     * @description: 判断Bean是否是单例
     * @date: 2019/5/31 16:26
     * @param: [beanName] Bean的名称
     * @return: boolean true为是单例
     */
    public static boolean isSingleton(String beanName) {
        return getApplicationContext().isSingleton(beanName);
    }

    /**
     * @author: Ares
     * @description: 获取Bean的Class
     * @date: 2019/5/31 16:28
     * @param: [beanName] Bean的名称
     * @return: java.lang.Class Class
     */
    public static Class getType(String beanName) {
        return getApplicationContext().getType(beanName);
    }

    /**
     * @author: Ares
     * @description: 通过类型获取所有bean
     * @date: 2019/6/1 14:23
     * @param: [clazz] 请求参数
     * @return: java.util.Map<java.lang.String
     */
    public static <T> Map<String, T> getBeansOfType(Class<T> clazz) {
        return getApplicationContext().getBeansOfType(clazz);
    }

    /**
     * @author: Ares
     * @description: 获取所有的Bean
     * @date: 2019/6/11 15:32
     * @param: [] 请求参数
     * @return: java.util.List<java.lang.Class ?>> 响应参数
     */
    public static List<Class<?>> getAllBeans() {
        List<Class<?>> result = new LinkedList<>();
        String[] beans = getApplicationContext().getBeanDefinitionNames();
        for (String beanName : beans) {
            Class<?> clazz = getType(beanName);
            result.add(clazz);
        }
        return result;
    }

    /**
     * @author: Ares
     * @description: 根据前缀获取spring的配置信息(不保留前缀)
     * @date: 2020/3/19 0:15
     * @param: [environment, prefix] 环境信息实体, 前缀
     * @return: java.util.Properties 响应参数
     */
    public static Properties getPropertiesByPrefix(Environment environment, String prefix) {
        return getPropertiesByPrefix(environment, prefix, false);
    }

    /**
     * @author: Ares
     * @description: 根据前缀获取spring的配置信息
     * @date: 2020/3/19 0:15
     * @param: [environment, prefix, retainPrefix] 环境信息实体, 前缀, 是否保留前缀
     * @return: java.util.Properties 响应参数
     */
    public static Properties getPropertiesByPrefix(Environment environment, String prefix,
        boolean retainPrefix) {
        Properties properties = new Properties();
        StandardEnvironment standardEnvironment = (StandardEnvironment) environment;
        standardEnvironment.getPropertySources().forEach(propertySource -> {
            if (propertySource instanceof MapPropertySource) {
                MapPropertySource mapPropertySource = (MapPropertySource) propertySource;
                mapPropertySource.getSource().forEach((key, value) -> {
                    if (key.startsWith(prefix)) {
                        String propertyKey = retainPrefix ? key : key.replace(prefix + ".", "");
                        properties.put(propertyKey, value);
                        properties.setProperty(propertyKey, String.valueOf(value));
                    }
                });
            }
        });
        return properties;
    }

    /**
     * @author: Ares
     * @description: 容器启动完成后在运行时根据前缀获取spring的配置信息
     * @date: 2020/3/19 0:15
     * @param: [prefix, retainPrefix] 前缀, 是否保留前缀
     * @return: java.util.Properties 响应参数
     */
    public static Properties getPropertiesByPrefixOnRuntime(String prefix, boolean retainPrefix) {
        return getPropertiesByPrefix(applicationContext.getEnvironment(), prefix, retainPrefix);
    }

    /**
     * @author: Ares
     * @description: 容器启动完成后在运行时根据前缀获取spring的配置信息(不保留前缀)
     * @date: 2020/3/19 0:15
     * @param: [prefix] 前缀
     * @return: java.util.Properties 响应参数
     */
    public static Properties getPropertiesByPrefixOnRuntime(String prefix) {
        return getPropertiesByPrefixOnRuntime(prefix, false);
    }

}
