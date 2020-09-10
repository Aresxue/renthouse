package com.asiainfo.portal.controller;

import com.asiainfo.frame.base.BaseRequest;
import com.asiainfo.frame.base.CommonResponse;
import com.asiainfo.strategy.function.SharingStrategyFuncService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author: Ares
 * @date: 2019/6/17 10:53
 * @description: 测试远程调用
 * @version: JDK 1.8
 */
@RestController
@RequestMapping("/remote")
public class RemoteController
{
    @Autowired
    SharingStrategyFuncService sharingStrategyFuncService;


    @RequestMapping("/test")
    public CommonResponse test(BaseRequest request)
    {
        return sharingStrategyFuncService.sharingStrategy(request);
    }
}
