package com.asiainfo.strategy.utils;

import com.asiainfo.frame.utils.NameThreadFactory;
import com.asiainfo.frame.utils.SpringUtil;
import com.asiainfo.strategy.config.HttpConnectionPoolConfig;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.NameValuePair;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.lang.NonNull;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * @author: Ares
 * @date: 2019/2/26 1:31
 * @description: Http请求工具类
 * 请求由连接池维护
 * @version: JDK 1.8
 */
public class HttpClientUtil implements ApplicationListener<ContextRefreshedEvent>
{
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpClientUtil.class);

    @Autowired
    private static HttpConnectionPoolConfig config;

    /**
     * 端口和ip的分割符
     */
    private static final String IP_AND_PORT_SPLIT = ":";
    /**
     * Http请求客户端, 使用下方线程锁实现单例
     */
    private static CloseableHttpClient httpClient = null;
    /**
     * Http连接池管理对象
     */
    private static PoolingHttpClientConnectionManager manager = null;
    /**
     * 监控Http连接池中的空闲和异常连接
     */
    private static ScheduledExecutorService monitorExecutor = null;
    /**
     * 相当于线程锁, 用于线程安全
     */
    private static final Object SYNC_LOCK = new Object();

    /**
     * @author: Ares
     * @description: 配置http请求对象的消息头
     * @date: 2019/11/01 16:21
     * @param: [httpRequestBase] 请求基类
     * @return: void
     */
    private static void configRequest(HttpRequestBase httpRequestBase)
    {
        configRequest(httpRequestBase, Collections.emptyMap(), config.getTimeOut());
    }

    /**
     * @author: Ares
     * @description: 配置http请求对象的消息头和超时时间
     * @date: 2019/11/01 16:21
     * @param: [httpRequestBase, headers, timeOut]
     * 请求基类, 自定义消息头, 超时时间
     * @return: void
     */
    private static void configRequest(HttpRequestBase httpRequestBase, Map<String, String> headers, int timeOut)
    {
        // 设置公共Header等
        // httpRequestBase.setHeader("User-Agent", "Mozilla/5.0");
        // httpRequestBase.setHeader("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        // httpRequestBase.setHeader("Accept-Language","zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3");// "en-US,en;q=0.5");
        // httpRequestBase.setHeader("Accept-Charset","ISO-8859-1,utf-8,gbk,gb2312;q=0.7,*;q=0.7");
        httpRequestBase.setHeader("Content-Type", "application/json;charset=utf-8");

        // 根据外部传入参数设置消息头
        headers.forEach(httpRequestBase::setHeader);

        // 配置请求的超时设置
        RequestConfig requestConfig = RequestConfig.custom().setConnectionRequestTimeout(timeOut).setConnectTimeout(timeOut).setSocketTimeout(timeOut).build();
        httpRequestBase.setConfig(requestConfig);
    }

    /**
     * @author: Ares
     * @description: 获取Http请求客户端
     * @date: 2019/5/8 15:31
     * @param: [url] 请求地址
     * @return: org.apache.http.impl.client.CloseableHttpClient Http请求客户端
     **/
    private static CloseableHttpClient getHttpClient(String url)
    {
        String hostname = url.split("/")[2];
        int port = 80;
        if (hostname.contains(IP_AND_PORT_SPLIT))
        {
            String[] arr = hostname.split(IP_AND_PORT_SPLIT);
            hostname = arr[0];
            port = Integer.parseInt(arr[1]);
        }
        // 双重检查
        if (null == httpClient)
        {
            synchronized (SYNC_LOCK)
            {
                if (null == httpClient)
                {
                    httpClient = createHttpClient(hostname, port);
                    // 开启监控线程, 对异常和空闲线程进行关闭
                    startMonitor();
                }
            }
        }
        return httpClient;
    }

    /**
     * @author: Ares
     * @description: 创建Http请求客户端
     * @date: 2019/5/8 15:32
     * @param: [hostname, port]
     * 主机名, 端口号
     * @return: org.apache.http.impl.client.CloseableHttpClient Http请求对象
     **/
    private static CloseableHttpClient createHttpClient(String hostname, int port)
    {
        ConnectionSocketFactory csf = PlainConnectionSocketFactory.getSocketFactory();
        LayeredConnectionSocketFactory lcsf = SSLConnectionSocketFactory.getSocketFactory();
        Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create().register("http", csf).register("https", lcsf).build();
        manager = new PoolingHttpClientConnectionManager(registry);
        // 设置最大连接数, 默认20
        manager.setMaxTotal(config.getMaxTotal());
        // 设置每个路由默认的最大连接数, 默认2
        manager.setDefaultMaxPerRoute(config.getMaxPerRoute());
        HttpHost httpHost = new HttpHost(hostname, port);
        // 设置路由的最大连接数,优先于DefaultMaxPerRoute
        manager.setMaxPerRoute(new HttpRoute(httpHost), config.getMaxRoute());

        // 请求重试处理
        HttpRequestRetryHandler httpRequestRetryHandler = (exception, executionCount, context) -> {
            if (executionCount >= config.getRetryTime())
            {
                // 如果已经重试了3次就放弃, 默认的重试次数也是3次
                return false;
            }
            if (exception instanceof NoHttpResponseException)
            {
                // 如果服务器丢掉了连接，那么就重试
                return true;
            }
            if (exception instanceof SSLHandshakeException)
            {
                // 不要重试SSL握手异常
                return false;
            }
            if (exception instanceof InterruptedIOException)
            {
                // 超时
                return false;
            }
            if (exception instanceof UnknownHostException)
            {
                // 目标服务器不可达
                return false;
            }
            if (exception instanceof SSLException)
            {
                // SSL握手异常
                return false;
            }

            HttpClientContext clientContext = HttpClientContext.adapt(context);
            HttpRequest request = clientContext.getRequest();
            // 如果请求是幂等的,就再次尝试
            return !(request instanceof HttpEntityEnclosingRequest);
        };

        return HttpClients.custom().setConnectionManager(manager).setRetryHandler(httpRequestRetryHandler).build();
    }


    /**
     * @author: Ares
     * @description: Post请求需要设置消息体
     * @date: 2019/8/16 16:56
     * @param: [httpPost, bodyParams]
     * Post请求对象, 消息体请求参数
     * @return: void 响应参数
     */
    private static void setPostParams(
            HttpPost httpPost, Map<String, Object> bodyParams)
    {
        List<NameValuePair> data = new ArrayList<>();
        Set<String> keySet = bodyParams.keySet();
        for (String key : keySet)
        {
            data.add(new BasicNameValuePair(key, bodyParams.get(key).toString()));
        }
        httpPost.setEntity(new UrlEncodedFormEntity(data, StandardCharsets.UTF_8));
    }

    /**
     * @author: Ares
     * @description: Post请求URL获取内容
     * @date: 2019/5/8 15:38
     * @param: [url, params]
     * 请求地址, 请求参数
     * @return: java.lang.String 响应内容
     **/
    public static String post(String url, Map<String, Object> params) throws Exception
    {
        HttpPost httpPost = new HttpPost(url);
        configRequest(httpPost);
        return request(httpPost, url, params, true);
    }

    /**
     * @author: Ares
     * @description: Post请求URL获取内容
     * 支持传入超时时间
     * 支持传入消息头
     * @date: 2019/5/8 15:38
     * @param: [url, params, headers, timeOut]
     * 请求地址, 请求参数, 自定义消息头, 超时时间
     * @return: java.lang.String 响应内容
     **/
    public static String post(String url, Map<String, Object> params, Map<String, String> headers, int timeOut) throws Exception
    {
        HttpPost httpPost = new HttpPost(url);
        configRequest(httpPost, headers, timeOut);
        return request(httpPost, url, params, true);
    }

    /**
     * @author: Ares
     * @description: Post请求URL获取内容
     * 支持传入消息头
     * @date: 2019/11/1 16:26
     * @param: [url, params, headers]
     * 请求地址, 请求参数, 自定义消息头
     * @return: java.lang.String 响应参数
     */
    public static String post(String url, Map<String, Object> params, Map<String, String> headers) throws Exception
    {
        return post(url, params, headers, config.getTimeOut());
    }

    /**
     * @author: Ares
     * @description: Post请求URL获取内容
     * 支持传入超时时间
     * @date: 2019/11/1 16:26
     * @param: [url, params, timeout]
     * 请求地址, 请求参数, 超时时间
     * @return: java.lang.String 响应参数
     */
    public static String post(String url, Map<String, Object> params, int timeout) throws Exception
    {
        return post(url, params, Collections.emptyMap(), timeout);
    }

    /**
     * @author: Ares
     * @description: Get请求URL获取内容
     * @date: 2019/5/8 15:39
     * @param: [url] 请求地址
     * @return: java.lang.String 响应内容
     **/
    public static String get(String url) throws Exception
    {
        HttpGet httpGet = new HttpGet(url);
        configRequest(httpGet);
        return request(httpGet, url, null, false);
    }

    /**
     * @author: Ares
     * @description: Get请求URL获取内容
     * 支持传入消息头
     * 支持传入超时时间
     * @date: 2019/11/01 16:30
     * @param: [url, headers, timeout]
     * 请求地址, 自定义消息头, 超时时间
     * @return: java.lang.String 响应内容
     **/
    public static String get(String url, Map<String, String> headers, int timeout) throws Exception
    {
        HttpGet httpGet = new HttpGet(url);
        configRequest(httpGet, headers, timeout);
        return request(httpGet, url, null, false);
    }

    /**
     * @author: Ares
     * @description: Get请求URL获取内容
     * 支持传入消息头
     * @date: 2019/11/01 16:30
     * @param: [url, headers]
     * 请求地址, 自定义消息头
     * @return: java.lang.String 响应内容
     **/
    public static String get(String url, Map<String, String> headers) throws Exception
    {
        HttpGet httpGet = new HttpGet(url);
        configRequest(httpGet, headers, config.getTimeOut());
        return request(httpGet, url, null, false);
    }

    /**
     * @author: Ares
     * @description: Get请求URL获取内容
     * 支持传入超时时间
     * @date: 2019/11/01 16:30
     * @param: [url, timeout]
     * 请求地址, 超时时间
     * @return: java.lang.String 响应内容
     **/
    public static String get(String url, int timeout) throws Exception
    {
        HttpGet httpGet = new HttpGet(url);
        configRequest(httpGet, Collections.emptyMap(), timeout);
        return request(httpGet, url, null, false);
    }

    /**
     * @author: Ares
     * @description: 请求通用代码
     * @date: 2019/5/8 15:46
     * @param: [requestBase, url, requestParams, isPost]
     * 请求基类, 请求地址, 请求参数, 是否为Post请求
     * @return: java.lang.String 响应参数
     **/
    private static String request(HttpRequestBase requestBase, String url, Map<String, Object> requestParams, boolean isPost) throws Exception
    {
        if (isPost)
        {
            setPostParams((HttpPost) requestBase, requestParams);
        }
        CloseableHttpResponse response = null;
        try
        {
            response = getHttpClient(url).execute(requestBase, HttpClientContext.create());
            HttpEntity entity = response.getEntity();
            String result = EntityUtils.toString(entity, StandardCharsets.UTF_8);
            // 关闭输入流
            EntityUtils.consume(entity);
            return result;
        } finally
        {
            try
            {
                if (null != response)
                {
                    response.close();
                }
            } catch (IOException e)
            {
                LOGGER.error("关闭响应对象时发生异常: ", e);
            }
        }
    }

    /**
     * @author: Ares
     * @description: 开启监控线程, 对异常和空闲线程进行关闭
     * @date: 2019/8/17 9:49
     * @param: [] 请求参数
     * @return: void 响应参数
     */
    private static void startMonitor()
    {
        // 使用命名的线程工厂, 在排查问题有标识性
        ThreadFactory monitorHttpConnectPoolFactory = new NameThreadFactory().setNameFormat(config.getMonitorThreadFactoryName()).build();
        monitorExecutor = new ScheduledThreadPoolExecutor(config.getMonitorThreadNum(), monitorHttpConnectPoolFactory);
        MonitorHttpWorker worker = new MonitorHttpWorker(manager, config);
        monitorExecutor.scheduleAtFixedRate(worker, config.getHttpMonitorPeriod(), config.getHttpMonitorPeriod(), TimeUnit.MILLISECONDS);
    }

    /**
     * @author: Ares
     * @description: 关闭连接池
     * @date: 2019/8/17 9:50
     * @param: [] 请求参数
     * @return: void 响应参数
     */
    public static void closeHttpConnectionPool()
    {
        try
        {
            httpClient.close();
            manager.close();
            monitorExecutor.shutdown();
        } catch (IOException e)
        {
            LOGGER.error("关闭连接池时发生异常: ", e);
        }
    }


    @Override
    public void onApplicationEvent(@NonNull ContextRefreshedEvent contextRefreshedEvent)
    {
        // 如果自动注入失败那么手动注入
        if (null == config)
        {
            config = (HttpConnectionPoolConfig) SpringUtil.getBean(HttpConnectionPoolConfig.class);
        }
    }
}

class MonitorHttpWorker implements Runnable
{
    private static final Logger LOGGER = LoggerFactory.getLogger(MonitorHttpWorker.class);

    private HttpConnectionPoolConfig config;

    private PoolingHttpClientConnectionManager manager;

    /**
     * 线程名称
     */
    private String threadName;

    MonitorHttpWorker(PoolingHttpClientConnectionManager manager, HttpConnectionPoolConfig config)
    {
        super();
        this.manager = manager;
        this.config = config;
        this.threadName = config.getMonitorThreadName();
    }

    public String getThreadName()
    {
        return threadName;
    }

    /**
     * @author: Ares
     * @description: Http连接监控
     * 关闭异常和空闲连接
     * @date: 2019/8/17 9:39
     * @param: [] 请求参数
     * @return: void 响应参数
     */
    @Override
    public void run()
    {
        // 关闭异常连接
        manager.closeExpiredConnections();
        // 关闭空闲的连接
        manager.closeIdleConnections(config.getHttpIdleTimeOut(), TimeUnit.MILLISECONDS);
        LOGGER.info("关闭异常或空闲{}ms以上的连接", config.getHttpIdleTimeOut());
    }
}
