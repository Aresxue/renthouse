package com.asiainfo.strategy.aop;

import com.asiainfo.frame.vo.ResponseBase;
import com.asiainfo.strategy.annotations.ServiceLimit;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static com.asiainfo.frame.constants.ResponseEnum.INVOKE_SPEED_OVER_QUICK;

/**
 * @author: Ares
 * @date: 2020/5/14 21:08
 * @description: 服务限速
 * @version: JDK 1.8
 */

@Aspect
@Configuration
public class ServiceLimitAspect
{
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceLimitAspect.class);

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 方法缓存
     */
    private final Map<String, Method> methodCache = new ConcurrentHashMap<>();

    /**
     * 本地缓存计数器
     */
    private final LoadingCache<Long, AtomicLong> counter = CacheBuilder.newBuilder()
            // 设置并发级别为cpu核心数，并发级别是指可以同时写缓存的线程数
            .concurrencyLevel(Runtime.getRuntime().availableProcessors() * 2 - 1)
            // 过期时间设为1分钟, 需要注意限制period要小于该值
            .expireAfterWrite(1, TimeUnit.MINUTES).build(new CacheLoader<Long, AtomicLong>()
            {
                @Override
                public AtomicLong load(@NonNull Long seconds)
                {
                    return new AtomicLong(0);
                }
            });

    @Around("@annotation(serviceLimit)")
    public Object serviceLimit(ProceedingJoinPoint joinPoint, ServiceLimit serviceLimit) throws Throwable
    {
        String methodName = joinPoint.getSignature().getName();

        Method method = methodCache.get(methodName);
        if (null == method)
        {
            //获取方法返回值类型
            Object[] args = joinPoint.getArgs();
            Class<?>[] paramsClazz = new Class<?>[args.length];
            for (int i = 0; i < args.length; ++i)
            {
                paramsClazz[i] = args[i].getClass();
            }
            // 获取方法
            method = joinPoint.getTarget().getClass().getDeclaredMethod(methodName, paramsClazz);
            methodCache.put(methodName, method);
        }

        // 获取返回值类型
        Type returnType = method.getAnnotatedReturnType().getType();

        // 时间周期
        long period = Long.parseLong(serviceLimit.period());
        // 调用限制次数
        long permitTimes = Long.parseLong(serviceLimit.permitTimes());
        long currentSeconds = System.currentTimeMillis() / period;
        if (counter.get(currentSeconds).incrementAndGet() > permitTimes)
        {
            LOGGER.warn(INVOKE_SPEED_OVER_QUICK.getLoggerDesc(), methodName);
            String response = objectMapper.writeValueAsString(new ResponseBase(INVOKE_SPEED_OVER_QUICK));
            Class<?> clazz = Class.forName(returnType.getTypeName());
            return objectMapper.readValue(response, clazz);
        }
        return joinPoint.proceed();
    }
}
