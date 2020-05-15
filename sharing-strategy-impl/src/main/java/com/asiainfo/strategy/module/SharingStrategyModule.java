package com.asiainfo.strategy.module;

import com.asiainfo.frame.constants.ResponseEnum;
import com.asiainfo.frame.vo.RequestBase;
import com.asiainfo.frame.vo.ResponseBase;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author: Ares
 * @date: 2020/4/7 14:08
 * @description: 合租攻略业务层实现
 * @version: JDK 1.8
 */
@Component
public class SharingStrategyModule
{
    @Transactional(rollbackFor = Exception.class)
    public ResponseBase sharingStrategy(RequestBase requestBase)
    {
        ResponseBase response = new ResponseBase();
        response.setResponseEnum(ResponseEnum.SUCCESS);
        return response;
    }
}
