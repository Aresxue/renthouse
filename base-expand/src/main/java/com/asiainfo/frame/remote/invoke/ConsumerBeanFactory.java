package com.asiainfo.frame.remote.invoke;

import com.asiainfo.frame.utils.SpringUtil;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.stereotype.Component;

/**
 * @author: Ares
 * @date: 2020/4/28 17:20
 * @description: 代理bean工厂
 * @version: JDK 1.8
 */
@Component
public class ConsumerBeanFactory implements FactoryBean<Object>
{
    /**
     * 服务接口
     */
    private Class<?> interfaceClass;
    /**
     * 服务接口名
     */
    private String interfaceName;
    /**
     * 内部服务节点名
     */
    private String center;
    /**
     * 组别
     */
    private String group;
    /**
     * 版本
     */
    private String version;


    private ConsumerInvoke consumerInvoke;

    @Override
    public Object getObject()
    {
        if(null == consumerInvoke){
            consumerInvoke = SpringUtil.getBean(ConsumerInvoke.class);
        }
        return consumerInvoke.invoke(interfaceClass, interfaceName, center, group, version);
    }

    @Override
    public Class<?> getObjectType()
    {
        return interfaceClass;
    }

    @Override
    public boolean isSingleton()
    {
        return true;
    }

    public Class<?> getInterfaceClass()
    {
        return interfaceClass;
    }

    public void setInterfaceClass(Class<?> interfaceClass)
    {
        this.interfaceClass = interfaceClass;
    }

    public String getInterfaceName()
    {
        return interfaceName;
    }

    public void setInterfaceName(String interfaceName)
    {
        this.interfaceName = interfaceName;
    }

    public String getCenter()
    {
        return center;
    }

    public void setCenter(String center)
    {
        this.center = center;
    }

    public String getGroup()
    {
        return group;
    }

    public void setGroup(String group)
    {
        this.group = group;
    }

    public String getVersion()
    {
        return version;
    }

    public void setVersion(String version)
    {
        this.version = version;
    }
}
