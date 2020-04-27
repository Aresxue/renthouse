package com.asiainfo.strategy.function.impl;

import com.asiainfo.frame.annotations.RemoteService;
import com.asiainfo.frame.base.RequestBase;
import com.asiainfo.frame.base.ResponseBase;
import com.asiainfo.frame.base.ResponseEnum;
import com.asiainfo.strategy.business.SharingStrategyBusService;
import com.asiainfo.strategy.function.SharingStrategyFuncService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @author: Ares
 * @date: 2019/6/17 10:19
 * @description: 合租攻略实现
 * @version: JDK 1.8
 */
@RemoteService
@Service
public class SharingStrategyFuncServiceImpl implements SharingStrategyFuncService
{
    private static final Logger LOGGER = LoggerFactory.getLogger(SharingStrategyFuncServiceImpl.class);

    @Autowired
    private SharingStrategyBusService sharingStrategyBusService;

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
        LOGGER.info("调用功能服务");
        ResponseBase response = new ResponseBase();
        response.setResponseEnum(ResponseEnum.SUCCESS);
        return response;
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
        request.forEach(i -> {
            i.forEach((k, v) -> {
                LOGGER.info(k + ":" + v);
            });
        });
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
}
