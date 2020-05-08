package com.asiainfo.portal.controller;

import com.asiainfo.frame.annotations.AresConsumer;
import com.asiainfo.frame.vo.RequestBase;
import com.asiainfo.frame.vo.ResponseBase;
import com.asiainfo.strategy.service.SharingStrategyService;
import org.springframework.web.bind.annotation.RequestBody;
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
//    @Autowired
    @AresConsumer(center = "sharing-strategy-impl", group = "ares", version = "1.0.0")
    private SharingStrategyService sharingStrategyService;

    @RequestMapping("/test")
    public ResponseBase test(@RequestBody RequestBase request)
    {
        return sharingStrategyService.sharingStrategy(request);

//        Map map = new HashMap();
//        List<RequestBase> temp = new ArrayList<>();
//        temp.add(request);
//        map.put("happy", temp);
//        return sharingStrategyService.sharingStrategy(map);

//        Map tempMap = new HashMap<>();
//        tempMap.put(request.getUserName(),request.getPassword());
//        List list = new ArrayList();
//        list.add(tempMap);
//        return sharingStrategyService.sharingStrategy(list);

//        return sharingStrategyService.sharingStrategy(1);
    }
}
