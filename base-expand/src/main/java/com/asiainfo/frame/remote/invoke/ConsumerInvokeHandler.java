package com.asiainfo.frame.remote.invoke;

import com.asiainfo.frame.utils.SpringUtil;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author: Ares
 * @date: 2020/4/28 17:04
 * @description: 进行反射调用
 * @version: JDK 1.8
 */
public class ConsumerInvokeHandler implements InvocationHandler
{
    private String consumerBeanName;
    private Object bean;

    public ConsumerInvokeHandler(String consumerBeanName)
    {
        this.consumerBeanName = consumerBeanName;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
    {
        try
        {
            if (null == bean)
            {
                bean = SpringUtil.getBean(consumerBeanName);
            }
            return method.invoke(bean, args);
        } catch (InvocationTargetException var6)
        {
            throw var6.getTargetException();
        }
    }

}
