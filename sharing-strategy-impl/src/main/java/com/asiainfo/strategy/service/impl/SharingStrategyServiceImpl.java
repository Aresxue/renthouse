package com.asiainfo.strategy.service.impl;

import com.asiainfo.frame.annotations.AresProvider;
import com.asiainfo.frame.vo.RequestBase;
import com.asiainfo.frame.vo.ResponseBase;
import com.asiainfo.frame.vo.ResponseEnum;
import com.asiainfo.strategy.module.SharingStrategyModule;
import com.asiainfo.strategy.service.SharingStrategyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author: Ares
 * @date: 2019/6/17 10:19
 * @description: 合租攻略实现
 * @version: JDK 1.8
 */
@Service
@AresProvider(group = "ares", version = "1.0.0")
public class SharingStrategyServiceImpl implements SharingStrategyService
{
    private static final Logger LOGGER = LoggerFactory.getLogger(SharingStrategyServiceImpl.class);

//    @Autowired
    private SharingStrategyModule sharingStrategyModule;

    /**
     * @author: Ares
     * @description: 合租攻略
     * @date: 2019/6/17 10:20
     * @Param: [request] 请求参数
     * @return: ResponseBase 响应参数
     */
    @Override
    public ResponseBase sharingStrategy(RequestBase request)
    {
        LOGGER.info("调用接口服务");
        return  sharingStrategyModule.sharingStrategy(request);
    }

    @Override
    public ResponseBase sharingStrategy(Map<String, List<RequestBase>> request)
    {
        request.forEach((k, list) -> list.forEach(i -> LOGGER.info(i.getUserName() + ":" + i.getPassword())));
        ResponseBase response = new ResponseBase();
        response.setResponseEnum(ResponseEnum.SUCCESS);
        return response;
    }

    @Override
    public ResponseBase sharingStrategy(List<Map<String, String>> request)
    {
        request.forEach(i -> i.forEach((k, v) -> LOGGER.info(k + ":" + v)));
        ResponseBase response = new ResponseBase();
        response.setResponseEnum(ResponseEnum.SUCCESS);
        return response;
    }

    @Override
    public ResponseBase sharingStrategy(Integer i)
    {
        LOGGER.info(i.toString());
        ResponseBase response = new ResponseBase();
        response.setResponseEnum(ResponseEnum.SUCCESS);
        return response;
    }

    public static void main(String[] args) throws Throwable
    {
        SharingStrategyServiceImpl s = new SharingStrategyServiceImpl();
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        MethodType methodType = MethodType.methodType(ResponseBase.class, Integer.class);
        MethodHandle methodHandle = lookup.findVirtual(SharingStrategyServiceImpl.class, "sharingStrategy", methodType);
        Object[] params = new Object[1];
        List list = new ArrayList();
        list.add(1);
        params[0] = Integer.valueOf(1);
        methodHandle.invokeWithArguments(s, 1, 2);
    }
}
