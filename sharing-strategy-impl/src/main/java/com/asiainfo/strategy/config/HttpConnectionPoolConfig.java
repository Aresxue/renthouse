package com.asiainfo.strategy.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author: Ares
 * @date: 2019/8/16 17:25
 * @description: Http请求连接池配置
 * 需要在启动类添加扫描目录且Import
 * @version: JDK 1.8
 */
@ConfigurationProperties(prefix = "http.connection.pool")
public class HttpConnectionPoolConfig
{
    /**
     * 请求超时时间
     */
    @Value("${timeOut:10000}")
    private int timeOut;
    /**
     * 请求重试次数
     */
    @Value("${retryTime:3}")
    private int retryTime;
    /**
     * 连接池最大连接数, 默认20
     */
    @Value("${maxTotal:200}")
    private int maxTotal;
    /**
     * 每个路由默认的最大连接数, 默认2
     */
    @Value("${maxPerRoute:40}")
    private int maxPerRoute;
    /**
     * 每个路由的最大连接数,优先于maxPerRoute
     */
    @Value("${maxRoute:100}")
    private int maxRoute;
    /**
     * 监控线程池的线程数量
     */
    @Value("${maxRoute:1}")
    private int monitorThreadNum;
    /**
     * 空闲超时时间
     */
    @Value("${httpIdleTimeOut:10000}")
    private int httpIdleTimeOut;
    /**
     * 监控任务执行间隔
     */
    @Value("${httpMonitorPeriod:10000}")
    private int httpMonitorPeriod;
    /**
     * 监控连接池的线程工厂名称
     */
    @Value("${monitorThreadFactoryName:Http-Connection-Pool-Monitor-Thread-%d}")
    private String monitorThreadFactoryName = "Http-Connection-Pool-Monitor-Thread-%d";
    /**
     * 监控连接池的线程名称
     */
    @Value("${monitorThreadName:HttpConnectionMonitorWorker}")
    private String monitorThreadName = "HttpConnectionMonitorWorker";

    public int getTimeOut()
    {
        return timeOut;
    }

    public void setTimeOut(int timeOut)
    {
        this.timeOut = timeOut;
    }

    public int getRetryTime()
    {
        return retryTime;
    }

    public void setRetryTime(int retryTime)
    {
        this.retryTime = retryTime;
    }

    public int getMaxTotal()
    {
        return maxTotal;
    }

    public void setMaxTotal(int maxTotal)
    {
        this.maxTotal = maxTotal;
    }

    public int getMaxPerRoute()
    {
        return maxPerRoute;
    }

    public void setMaxPerRoute(int maxPerRoute)
    {
        this.maxPerRoute = maxPerRoute;
    }

    public int getMaxRoute()
    {
        return maxRoute;
    }

    public void setMaxRoute(int maxRoute)
    {
        this.maxRoute = maxRoute;
    }

    public int getMonitorThreadNum()
    {
        return monitorThreadNum;
    }

    public void setMonitorThreadNum(int monitorThreadNum)
    {
        this.monitorThreadNum = monitorThreadNum;
    }

    public int getHttpIdleTimeOut()
    {
        return httpIdleTimeOut;
    }

    public void setHttpIdleTimeOut(int httpIdleTimeOut)
    {
        this.httpIdleTimeOut = httpIdleTimeOut;
    }

    public int getHttpMonitorPeriod()
    {
        return httpMonitorPeriod;
    }

    public void setHttpMonitorPeriod(int httpMonitorPeriod)
    {
        this.httpMonitorPeriod = httpMonitorPeriod;
    }

    public String getMonitorThreadFactoryName()
    {
        return monitorThreadFactoryName;
    }

    public void setMonitorThreadFactoryName(String monitorThreadFactoryName)
    {
        this.monitorThreadFactoryName = monitorThreadFactoryName;
    }

    public String getMonitorThreadName()
    {
        return monitorThreadName;
    }

    public void setMonitorThreadName(String monitorThreadName)
    {
        this.monitorThreadName = monitorThreadName;
    }
}
