package com.asiainfo.portal.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: Ares
 * @date: 2019/5/31 15:11
 * @description: Eureka客户端信息获取
 * @version: JDK 1.8
 */
@RestController
@RequestMapping("/eurekaCenter")
public class EurekaController
{
    private static final Logger logger = LoggerFactory.getLogger(EurekaController.class);

    @Autowired
    private DiscoveryClient discoveryClient;

    /**
     * @author: Ares
     * @description: 获取注册在Eureka中的服务名称
     * @date: 2019/6/11 14:45
     * @Param: [] 请求参数
     * @return: java.util.List<java.lang.String> 响应参数
     */
    @GetMapping("/getEurekaServices")
    public List<String> getEurekaServices()
    {
        List<String> services = new ArrayList<>();
        List<String> serviceNames = discoveryClient.getServices();
        for (String serviceName : serviceNames)
        {
            List<ServiceInstance> serviceInstances = discoveryClient.getInstances(serviceName);
            for (ServiceInstance serviceInstance : serviceInstances)
            {
                services.add(String.format("%s:%s", serviceName, serviceInstance.getUri()));
            }
        }
        return services;
    }

}

