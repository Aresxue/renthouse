package com.asiainfo.strategy.function.impl;

import com.asiainfo.frame.annotations.RemoteService;
import com.asiainfo.frame.base.BaseRequest;
import com.asiainfo.frame.base.BaseResponse;
import com.asiainfo.frame.base.ResponseInfo;
import com.asiainfo.strategy.business.SharingStrategyBusService;
import com.asiainfo.strategy.function.SharingStrategyFuncService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
    private static final Logger logger = LoggerFactory.getLogger(SharingStrategyFuncServiceImpl.class);

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
    public BaseResponse sharingStrategy(BaseRequest request)
    {
        logger.info("调用功能服务");
        BaseResponse response = new BaseResponse();
        response.setResponseInfo(ResponseInfo.SUCCESS);
        return response;
    }
}
