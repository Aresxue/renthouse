package com.asiainfo.strategy.service.impl;

import com.asiainfo.strategy.service.ConditionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author: Ares
 * @date: 2020/6/4 13:56
 * @description:
 * @version: JDK 1.8
 */
public class ConditionServiceRemoteImpl implements ConditionService
{
    private static final Logger LOGGER = LoggerFactory.getLogger(ConditionServiceRemoteImpl.class);

    @Override
    public void doSomething()
    {
        LOGGER.info("我是远程服务");
    }
}
