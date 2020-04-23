package com.asiainfo.strategy.business.impl;

import com.asiainfo.strategy.business.TestProvider;
import com.asiainfo.strategy.mapper.TestMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

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

    @Autowired
    private TestMapper testMapper;

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void selectPaymentId(String datasourceId)
    {
        //        DynamicDataSourceUtil.changeDataSource(datasourceId);
        int value = testMapper.testDynamicDataSource();
        LOGGER.info("返回的payment_id为: {}", value);
        //        DynamicDataSourceUtil.clearDataSourceId();
    }


    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void testInsertPayment()
    {
        for (int i = 0; i < 100; i++)
        {
            testMapper.testInsertPayment(i);
        }
//        throw new RuntimeException();
    }
}
