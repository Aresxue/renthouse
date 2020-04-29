package com.asiainfo.portal.controller;

import com.asiainfo.frame.annotations.AresConsumer;
import com.asiainfo.frame.base.RequestBase;
import com.asiainfo.frame.base.ResponseBase;
import com.asiainfo.strategy.function.SharingStrategyFuncService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
//    @Autowired
    @AresConsumer(center = "sharing-strategy-impl", group = "ares", version = "1.0.0")
    private SharingStrategyFuncService sharingStrategyFuncService;

    @RequestMapping("/test")
    public ResponseBase test(@RequestBody RequestBase request)
    {
        //        return sharingStrategyFuncService.sharingStrategy(request);

        Map map = new HashMap();
        List<RequestBase> temp = new ArrayList<>();
        temp.add(request);
        map.put("happy", temp);
//        return sharingStrategyFuncService.sharingStrategy(map);

        Map tempMap = new HashMap<>();
        tempMap.put(request.getUserName(),request.getPassword());
        List list = new ArrayList();
        list.add(tempMap);
//        return sharingStrategyFuncService.sharingStrategy(list);

        return sharingStrategyFuncService.sharingStrategy(1);
    }
}
