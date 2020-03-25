package com.asiainfo.frame.remote.invoke;

import com.asiainfo.frame.annotations.RemoteInfc;
import com.asiainfo.frame.exceptions.RemoteInvokeException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.util.StringJoiner;

/**
 * @author: Ares
 * @date: 2019/6/10 17:43
 * @description: 发起远程调用并封装响应参数
 * @version: JDK 1.8
 */
@Component
public class RemoteInvokeHandler
{
    @Autowired
    private RestTemplate restTemplate;

    /**
     * 类加载器
     */
    private static final ClassLoader CLASSLOADER = RemoteInvokeHandler.class.getClassLoader();

    private static ObjectMapper objectMapper = new ObjectMapper();

    public <T> T remoteInvoke(final Class<T> remoteInfcClass, String serviceCenter)
    {
        @SuppressWarnings(value = "unchecked") T result = (T) Proxy.newProxyInstance(CLASSLOADER, new Class<?>[]{remoteInfcClass}, (proxy, method, args) -> {
            MultiValueMap<String, Object> remoteRequest = new LinkedMultiValueMap<>();

            RemoteInfc remoteInfc = remoteInfcClass.getAnnotation(RemoteInfc.class);
            if (null == remoteInfc)
            {
                throw new RemoteInvokeException(remoteInfcClass.getName() + "接口不是远程开放接口,请加上RemoteInfc注解");
            }

            // 远程接口服务名 + 服务版本号 + 远程方法 生成唯一标识
            StringJoiner uniqueKey = new StringJoiner(":");
            uniqueKey.add(remoteInfcClass.getName());
            // 接口的版本号
            uniqueKey.add(remoteInfc.version());
            uniqueKey.add(method.getName());
            Class<?>[] paramTypes = method.getParameterTypes();
            for (int i = 0; i < paramTypes.length; i++)
            {
                uniqueKey.add((paramTypes[i].getName()));
                // 放置方法请求参数
                remoteRequest.add("arg" + i, args[i]);
            }
            remoteRequest.add("uniqueKey", DigestUtils.md5DigestAsHex(uniqueKey.toString().getBytes(StandardCharsets.UTF_8)));

            // 发送http请求至其它内部实例,因为用的是实例名eureka会自动负载
            return restTemplate.postForObject("http://" + serviceCenter + "/common/innerInvoke", remoteRequest, method.getReturnType());
        });
        return result;
    }
}
