package com.asiainfo.strategy.business.impl;

import com.asiainfo.strategy.business.TestProvider;
import com.asiainfo.strategy.business.TestService;
import com.asiainfo.strategy.mapper.TestMapper;
import com.asiainfo.strategy.multiple.datasources.DynamicDataSourceUtil;
import com.asiainfo.strategy.multiple.datasources.TargetDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.asiainfo.strategy.multiple.datasources.DynamicDataSourceConstans.DEFAULT_TARGET_DATASOURCE;

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

    /**
     * @author: Ares
     * @description: 动态数据源测试
     * @date: 2020/3/24 10:07
     * @param: [] 请求参数
     * @return: int 响应参数
     */
    @Override
    public void testDynamicDataSource()
    {
        int value = testMapper.testDynamicDataSource();
        LOGGER.info("返回的payment_id为: {}", value);

        DynamicDataSourceUtil.changeDataSource("datasourceOne");
        value = testMapper.testDynamicDataSource();
        LOGGER.info("返回的payment_id为: {}", value);
        DynamicDataSourceUtil.clearDataSourceId();

        DynamicDataSourceUtil.changeDataSource("datasourceTwo");
        value = testMapper.testDynamicDataSource();
        DynamicDataSourceUtil.clearDataSourceId();
        LOGGER.info("返回的payment_id为: {}", value);

        DynamicDataSourceUtil.changeDataSource("datasourceOne");
        value = testMapper.testDynamicDataSource();
        LOGGER.info("返回的payment_id为: {}", value);
        DynamicDataSourceUtil.clearDataSourceId();

        DynamicDataSourceUtil.changeDataSource(DEFAULT_TARGET_DATASOURCE);
        value = testMapper.testDynamicDataSource();
        LOGGER.info("返回的payment_id为: {}", value);
        DynamicDataSourceUtil.clearDataSourceId();
    }


    /**
     * @author: Ares
     * @description: 有Transactional时使用DynamicDataSourceUtil.changeDataSource无法切换数据源
     * 原因是在DataSourceTransactionManager#doBegin前会把连接缓存下来, 下次直接使用该连接, 而不是重新获取
     * 这样就不会走AbstractRoutingDataSource的寻找数据源的操作
     * @date: 2020/3/26 14:22
     * @param: [] 请求参数
     * @return: void 响应参数
     */
    @Override
    @Transactional
    public void testDynamicDataSourceTransactional()
    {
        int value = testMapper.testDynamicDataSource();
        LOGGER.info("返回的payment_id为: {}", value);

        DynamicDataSourceUtil.changeDataSource("datasourceOne");
        value = testMapper.testDynamicDataSource();
        LOGGER.info("返回的payment_id为: {}", value);
        DynamicDataSourceUtil.clearDataSourceId();

        DynamicDataSourceUtil.changeDataSource("datasourceTwo");
        value = testMapper.testDynamicDataSource();
        DynamicDataSourceUtil.clearDataSourceId();
        LOGGER.info("返回的payment_id为: {}", value);

        DynamicDataSourceUtil.changeDataSource("datasourceOne");
        value = testMapper.testDynamicDataSource();
        LOGGER.info("返回的payment_id为: {}", value);
        DynamicDataSourceUtil.clearDataSourceId();

        DynamicDataSourceUtil.changeDataSource(DEFAULT_TARGET_DATASOURCE);
        value = testMapper.testDynamicDataSource();
        LOGGER.info("返回的payment_id为: {}", value);
        DynamicDataSourceUtil.clearDataSourceId();
    }

    /**
     * @author: Ares
     * @description: 使用方法包一层
     * 默认情况下依旧不会切换数据源
     * 传播级别改成总是新起一个事务也不会切换数据源, 原理如上, 换成别的隔离级别不报错但也无法切换数据源
     * <p>
     * 当把操作从内部方法变为一个业务的方法时有趣的事情发生了
     * 使用默认传播级别 即当当前存在事务即加入没有则新起时 依旧无法切换数据源
     * 使用REQUIRES_NEW时 使用DynamicDataSourceUtil.changeDataSource(datasourceId)依旧没法改变数据源, 但注解TargetDataSource设置的数据源生效了, 接着尝试了NESTED会直接报错, NOT_SUPPORTED也可以切换但是以非事务运行可用于查询
     * 对于原生的mysql, NESTED并不会报错但无法执行切换
     * @date: 2020/3/26 14:35
     * @param: [] 请求参数
     * @return: void 响应参数
     */
    @Override
    @Transactional
    public void testDynamicDataSourceTransactionalUseMethod()
    {
        int value = testMapper.testDynamicDataSource();
        LOGGER.info("返回的payment_id为: {}", value);

        selectPaymentId("datasourceOne");

        selectPaymentId("datasourceTwo");

        selectPaymentId("datasourceOne");

        selectPaymentId(DEFAULT_TARGET_DATASOURCE);

        testProvider.selectPaymentId("datasourceOne");

        testProvider.selectPaymentId("datasourceTwo");

        testProvider.selectPaymentId("datasourceOne");

        testProvider.selectPaymentId(DEFAULT_TARGET_DATASOURCE);

    }

    /**
     * @author: Ares
     * @description: 测试插入
     * 从查询结论可知还需调用新的服务中的方法X,且需要修改隔离级别
     * 如果是NOT_SUPPORTED就会看到当前所操作的数据数据库查不到, 但X所在的slave库已经可以看到一部分, 哪怕最后失败了, slave的数据也以全部写入不能回滚
     * 如果是REQUIRES_NEW或者NESTED则都会回滚, 两个数据库都不会写入数据
     * 如果不想内部的事务影响外部的事务, 在调用的地方try-catch即可
     * 分布式事务是数据库层面提供的, 只需要在数据库操作前调用即可
     * @date: 2020/3/26 10:15
     * @param: [value] 请求参数
     * @return: int 响应参数
     */
    @Override
    @Transactional
    @TargetDataSource
    public void testInsertPayment()
    {
        for (int i = 0; i < 100; i++)
        {
            testMapper.testInsertPayment(i);
        }

        //        DynamicDataSourceUtil.changeDataSource("datasourceOne");
        //        for (int i = 0; i < 100; i++)
        //        {
        //            testMapper.testInsertPayment(i);
        //        }


        testProvider.testInsertPayment();
        //        try
        //        {
        //            testProvider.testInsertPayment();
        //        } catch (Exception e)
        //        {
        //            e.printStackTrace();
        //        }
    }

    //    @Transactional
    @TargetDataSource(dataSourceId = "datasourceOne")
    public void selectPaymentId(String datasourceId)
    {
        DynamicDataSourceUtil.changeDataSource(datasourceId);
        int value = testMapper.testDynamicDataSource();
        LOGGER.info("返回的payment_id为: {}", value);
        DynamicDataSourceUtil.clearDataSourceId();
    }
}
