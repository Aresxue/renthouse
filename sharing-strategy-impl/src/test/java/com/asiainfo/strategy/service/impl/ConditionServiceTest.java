package com.asiainfo.strategy.service.impl;

import com.asiainfo.strategy.SharingStrategyImplApplication;
import com.asiainfo.strategy.service.ConditionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author: Ares
 * @date: 2020/6/4 14:08
 * @description:
 * @version: JDK 1.8
 */
@SpringBootTest(classes = SharingStrategyImplApplication.class)
public class ConditionServiceTest
{
    @Autowired
    private ConditionService conditionService;

    @Test
    public void testDoSomething()
    {
        conditionService.doSomething();
    }
}
