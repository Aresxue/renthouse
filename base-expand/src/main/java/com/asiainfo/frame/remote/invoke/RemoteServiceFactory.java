package com.asiainfo.frame.remote.invoke;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author: Ares
 * @date: 2019/6/1 17:38
 * @description: 代理bean的生产工厂
 * @version: JDK 1.8
 */
@Component
public class RemoteServiceFactory implements FactoryBean
{
    /**
     * 远程服务接口
     */
    private Class<?> remoteInfcClass;
    /**
     * 调用的内部服务节点名
     */
    private String serviceCenter;

    private final RemoteInvokeHandler remoteInvokeHandler;

    /**
     * 这里只能使用构造器注入
     */
    @Autowired
    public RemoteServiceFactory(RemoteInvokeHandler remoteInvokeHandler)
    {
        this.remoteInvokeHandler = remoteInvokeHandler;
    }

    public Class<?> getRemoteInfcClass()
    {
        return remoteInfcClass;
    }

    public void setRemoteInfcClass(Class<?> remoteInfcClass)
    {
        this.remoteInfcClass = remoteInfcClass;
    }

    public String getServiceCenter()
    {
        return serviceCenter;
    }

    public void setServiceCenter(String serviceCenter)
    {
        this.serviceCenter = serviceCenter;
    }

    @Override
    public Object getObject() throws Exception
    {
        return remoteInvokeHandler.remoteInvoke(remoteInfcClass, serviceCenter);
    }

    @Override
    public Class<?> getObjectType()
    {
        return remoteInfcClass;
    }

    @Override
    public boolean isSingleton()
    {
        return true;
    }
}
