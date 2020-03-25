package com.asiainfo.frame.invoke;

import com.asiainfo.frame.annotations.RemoteInfc;
import com.asiainfo.frame.annotations.RemoteService;
import com.asiainfo.frame.utils.DynamicProxyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ClassUtils;
import org.springframework.util.DigestUtils;

import javax.annotation.PostConstruct;
import java.beans.Introspector;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;

/**
 * @author: Ares
 * @date: 2019/6/1 14:18
 * @description: 远程服务调用扩展配置类
 * @version: JDK 1.8
 */
@Configuration
public class RemoteServiceConfig
{
    private static final Logger logger = LoggerFactory.getLogger(RemoteServiceConfig.class);

    @Autowired
    private ApplicationContext applicationContext;

    /**
     * @author: Ares
     * @description: 初始化方法
     * @date: 2019/6/1 14:19
     * @Param: [] 请求参数
     * @return: void 响应参数
     */
    @PostConstruct
    public void init() throws Exception
    {
        Set<Map.Entry<String, Object>> beanSet = applicationContext.getBeansOfType(Object.class).entrySet();
        for (Map.Entry<String, Object> beanEntry : beanSet)
        {
            Object bean = beanEntry.getValue();
            if (null == bean)
            {
                continue;
            }

            // 获取真实对象
            try
            {
                bean = DynamicProxyUtil.getTarget(bean);
            } catch (Exception e)
            {
                logger.error("获取真实对象时失败: ", e);
            }
            Class<?> beanClass = bean.getClass();

            // 防止field自动注入失败获取bean设置该field
            Field[] fields = beanClass.getDeclaredFields();
            for (Field field : fields)
            {
                RemoteInfc remoteInfc = field.getType().getAnnotation(RemoteInfc.class);
                if (null == remoteInfc)
                {
                    continue;
                }
                field.setAccessible(true);
                String beanName = Introspector.decapitalize(ClassUtils.getShortName(field.getType().getName()));

                Object object = null;
                try
                {
                    object = applicationContext.getBean(beanName);
                } catch (Exception e)
                {
                    object = applicationContext.getBean(field.getType());
                }
                bean = DynamicProxyUtil.getTarget(bean);
                field.set(bean, object);
            }


            RemoteService remoteService = beanClass.getAnnotation(RemoteService.class);
            if (null != remoteService)
            {
                Class<?>[] interfaces = beanClass.getInterfaces();

                for (Class<?> infc : interfaces)
                {
                    RemoteInfc remoteInfc = infc.getAnnotation(RemoteInfc.class);
                    if (null == remoteInfc)
                    {
                        logger.warn("接口{}缺少RemoteInfc注解不生成代理", infc.getName());
                        continue;
                    }
                    Method[] methods = infc.getMethods();
                    for (Method method : methods)
                    {
                        RemoteProxyService proxyService = new RemoteProxyService();
                        proxyService.setProxyService(bean);
                        proxyService.setProxyMethod(method);

                        StringJoiner uniqueKey = new StringJoiner(":");
                        uniqueKey.add(infc.getName());
                        // 服务实现的版本号
                        uniqueKey.add(remoteService.version());
                        uniqueKey.add(method.getName());
                        Class<?>[] paramTypes = method.getParameterTypes();
                        for (int i = 0; i < paramTypes.length; i++)
                        {
                            uniqueKey.add((paramTypes[i].getName()));
                        }
                        CommonController.addProxyMethod(DigestUtils.md5DigestAsHex(uniqueKey.toString().getBytes(StandardCharsets.UTF_8)), proxyService);
                    }
                }
            }
        }
    }
}
