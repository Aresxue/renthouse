package com.asiainfo.strategy.function;


import com.asiainfo.frame.annotations.RemoteInfc;
import com.asiainfo.frame.base.RequestBase;
import com.asiainfo.frame.base.ResponseBase;

import java.util.List;
import java.util.Map;

/**
 * @author: Ares
 * @date: 2019/6/13 15:28
 * @description: 合租攻略接口
 * @version: JDK 1.8
 */
@RemoteInfc
public interface SharingStrategyFuncService
{
    /**
     * @author: Ares
     * @description: 合租攻略
     * @date: 2019/6/17 10:20
     * @Param: [request] 请求参数
     * @return: ResponseBase 响应参数
     */
    ResponseBase sharingStrategy(RequestBase request);

    ResponseBase sharingStrategy(Map<String, List<RequestBase>> request);
    ResponseBase sharingStrategy(List<Map<String, String>> request);

}
