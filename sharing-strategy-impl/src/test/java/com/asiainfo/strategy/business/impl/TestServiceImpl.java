package com.asiainfo.strategy.business.impl;

import com.asiainfo.strategy.business.TestProvider;
import com.asiainfo.strategy.business.TestService;
import com.asiainfo.strategy.mapper.TestMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author: Ares
 * @date: 2020/3/24 10:06
 * @description: 测试业务层实现, 用来测试多数据源功能以及其它功能
 * @version: JDK 1.8
 */
@Service
public class TestServiceImpl implements TestService
{
    private static final Logger LOGGER = LoggerFactory.getLogger(TestServiceImpl.class);

    @Autowired
    private TestMapper testMapper;

    @Autowired
    private TestProvider testProvider;

}
