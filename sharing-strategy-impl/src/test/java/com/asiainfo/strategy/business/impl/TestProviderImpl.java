package com.asiainfo.strategy.business.impl;

import com.asiainfo.strategy.business.TestProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * @author: Ares
 * @date: 2020/3/26 14:44
 * @description: 提供接口被TestService调用
 * @version: JDK 1.8
 */
@Service
public class TestProviderImpl implements TestProvider
{
    private static final Logger LOGGER = LoggerFactory.getLogger(TestServiceImpl.class);

}