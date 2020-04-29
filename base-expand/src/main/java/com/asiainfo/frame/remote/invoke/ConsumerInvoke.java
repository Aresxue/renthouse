package com.asiainfo.frame.remote.invoke;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.StringJoiner;
import java.util.stream.Collectors;

/**
 * @author: Ares
 * @date: 2020/4/28 17:04
 * @description: 发起远程调用并封装响应参数
 * @version: JDK 1.8
 */
@Component
public class ConsumerInvoke
{

    @Autowired
    private RestTemplate restTemplate;

    /**
     * 类加载器
     */
    private static final ClassLoader CLASSLOADER = ConsumerBeanFactory.class.getClassLoader();

    public Object invoke(final Class<?> interfaceClass, String interfaceName, String center, String group, String version)
    {
        return Proxy.newProxyInstance(CLASSLOADER, new Class<?>[]{interfaceClass}, (proxy, method, args) -> {
            MultiValueMap<String, Object> remoteRequest = new LinkedMultiValueMap<>();

            // 接口名 + 方法名 + 组别 + 版本号 + 请求类型 生成唯一标识, 使用#连接
            StringJoiner uniqueKey = new StringJoiner("#");
            uniqueKey.add(null == interfaceClass ? interfaceName : interfaceClass.getName());
            uniqueKey.add(method.getName());
            uniqueKey.add(group);
            uniqueKey.add(version);
            Class<?>[] paramTypes = method.getParameterTypes();
            // 多个请求类型使用, 连接
            String parameterTypes = Arrays.stream(paramTypes).map(Class::getName).collect(Collectors.joining(","));
            uniqueKey.add(parameterTypes);
            for (int i = 0; i < paramTypes.length; i++)
            {
                // 放置方法请求参数
                remoteRequest.add("arg" + i, args[i]);
            }
            remoteRequest.add("uniqueKey", uniqueKey.toString());

            // 发送http请求至其它内部实例,因为用的是实例名eureka会自动负载
            return restTemplate.postForObject("http://" + center + "/provider/innerInvoke", remoteRequest, method.getReturnType());
        });
    }

}
