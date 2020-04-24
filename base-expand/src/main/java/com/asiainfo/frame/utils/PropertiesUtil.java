package com.asiainfo.frame.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

/**
 * @author: Ares
 * @date: 2020/4/23 20:34
 * @description:
 * @version: JDK 1.8
 */
public class PropertiesUtil
{
    private static final Logger LOGGER = LoggerFactory.getLogger(PropertiesUtil.class);

    /**
     * @author: Ares
     * @description: 从文件中获取所有参数
     * @date: 2020/4/23 20:39
     * @param: [fileName] 请求参数
     * @return: java.util.Properties 响应参数
     */
    public static Properties getAllProperties(String fileName)
    {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        URL url = classLoader.getResource(fileName);
        Properties properties = new Properties();
        if (null == url)
        {
            LOGGER.error("请检查文件名: {}", fileName);
            return properties;
        }
        try
        {
            properties.load(new FileInputStream(url.getPath()));
        } catch (IOException e)
        {
            LOGGER.error("请检查文件名: {}, 从文件中获取所有参数时失败: ", fileName, e);
        }
        return properties;
    }

    /**
     * @author: Ares
     * @description: 根据文件名和前缀获取配置信息
     * @date: 2020/4/23 20:41
     * @param: [fileName, prefix, retainPrefix]
     * 文件名, 前缀, 是否保留前缀
     * @return: java.util.Properties 响应参数
     */
    public static Properties getPropertiesByPrefix(String fileName, String prefix, boolean retainPrefix)
    {
        Properties properties = new Properties();
        getAllProperties(fileName).forEach((k, value) -> {
            String key = String.valueOf(k);
            if (key.startsWith(prefix))
            {
                String propertyKey = retainPrefix ? key : key.replace(prefix + ".", "");
                properties.put(propertyKey, value);
                properties.setProperty(propertyKey, String.valueOf(value));
            }
        });
        return properties;
    }

    /**
     * @author: Ares
     * @description: 根据前缀获取spring的配置信息(不保留前缀)
     * @date: 2020/4/23 20:52
     * @param: [fileName, prefix] 请求参数
     * @return: java.util.Properties 响应参数
     */
    public static Properties getPropertiesByPrefix(String fileName, String prefix)
    {
        return getPropertiesByPrefix(fileName, prefix, false);
    }

    /**
     * @author: Ares
     * @description: 根据文件名和key获取配置信息
     * @date: 2020/4/23 20:47
     * @param: [fileName, key]
     * 文件名, key
     * @return: java.util.Properties 响应参数
     */
    public static Object getPropertyByPrefix(String fileName, String key)
    {
        Properties properties = getAllProperties(fileName);
        Object object = properties.getProperty(key);
        if (object == null)
        {
            object = properties.get(key);
        }
        return object;
    }
}
