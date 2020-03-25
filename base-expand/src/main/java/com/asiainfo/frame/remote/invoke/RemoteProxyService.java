package com.asiainfo.frame.remote.invoke;

import java.lang.reflect.Method;

/**
 * @author: Ares
 * @date: 2019/6/1 16:36
 * @description: 远程代理服务
 * @version: JDK 1.8
 */
public class RemoteProxyService
{
    /**
     * 远程接口全名
     */
    private String remoteInfcName;
    /**
     * 服务中心名
     */
    private String serviceCenter;

    /**
     * 代理服务,服务实现bean
     */
    private Object proxyService;
    /**
     * 代理方法,服务实现中的方法
     */
    private Method proxyMethod;

    /**
     * 服务id,用于对外统一调用
     */
    private String serviceId;
    /**
     * 服务版本,用于对外统一调用
     */
    private String serviceVersion;
    /**
     * 方法名,用于对外统一调用
     */
    private String methodName;
    /**
     * 方法的请求类型,用于对外统一调用
     */
    private String requestType;

    public String getRemoteInfcName()
    {
        return remoteInfcName;
    }

    public void setRemoteInfcName(String remoteInfcName)
    {
        this.remoteInfcName = remoteInfcName;
    }

    public String getServiceCenter()
    {
        return serviceCenter;
    }

    public void setServiceCenter(String serviceCenter)
    {
        this.serviceCenter = serviceCenter;
    }

    public Object getProxyService()
    {
        return proxyService;
    }

    public void setProxyService(Object proxyService)
    {
        this.proxyService = proxyService;
    }

    public Method getProxyMethod()
    {
        return proxyMethod;
    }

    public void setProxyMethod(Method proxyMethod)
    {
        this.proxyMethod = proxyMethod;
    }

    public String getServiceId()
    {
        return serviceId;
    }

    public void setServiceId(String serviceId)
    {
        this.serviceId = serviceId;
    }

    public String getServiceVersion()
    {
        return serviceVersion;
    }

    public void setServiceVersion(String serviceVersion)
    {
        this.serviceVersion = serviceVersion;
    }

    public String getMethodName()
    {
        return methodName;
    }

    public void setMethodName(String methodName)
    {
        this.methodName = methodName;
    }

    public String getRequestType()
    {
        return requestType;
    }

    public void setRequestType(String requestType)
    {
        this.requestType = requestType;
    }
}
