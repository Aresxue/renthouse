package com.asiainfo.strategy.aop;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

import java.util.StringJoiner;

/**
 * @author Ares
 * @version JDK 1.8
 * @date 2018/12/5 17:39
 * @description: 将入参出参Json化打印
 */

@Aspect
@Configuration
public class PrintParamByJson
{
    private static final Logger LOGGER = LoggerFactory.getLogger(PrintParamByJson.class);

    /**
     * 参数类型和参数的连接符
     */
    public static final String ARG_TYPE_CONNECTOR = ": ";
    /**
     * 多个参数的分隔符
     */
    public static final String ARGS_DELIMITER = ",";

    /**
     * @author: Ares
     * @description: 环绕通知, 环绕增强, 相当于MethodInterceptor
     * @date: 2019/6/6 13:54
     * @param: [thisJoinPoint] 请求参数
     * @return: java.lang.Object 响应参数
     **/
    @Around("com.asiainfo.strategy.aop.CommonJoinPointConfig.printParamByJson()")
    public Object printParamByJson(ProceedingJoinPoint thisJoinPoint) throws Throwable
    {
        Object result = null;

        ObjectMapper mapper = new ObjectMapper();
        // 忽略null值
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        StringJoiner stringJoiner = new StringJoiner(ARGS_DELIMITER);
        Signature signature = thisJoinPoint.getSignature();
        Object[] args = thisJoinPoint.getArgs();
        for (Object arg : args)
        {
            stringJoiner.add((null == arg ? "" : arg.getClass().getSimpleName() + ARG_TYPE_CONNECTOR) + mapper.writeValueAsString(arg));
        }

        LOGGER.info("方法{}入参为: {}", signature.getName(), stringJoiner);
        result = thisJoinPoint.proceed();
        LOGGER.info("方法{}出参为: {}", signature.getName(), (null == result ? "" : result.getClass().getSimpleName() + ARG_TYPE_CONNECTOR) + mapper.writeValueAsString(result));

        return result;
    }

}
