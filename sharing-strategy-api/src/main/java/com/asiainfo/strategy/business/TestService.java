package com.asiainfo.strategy.business;

/**
 * @author: Ares
 * @date: 2020/3/24 10:04
 * @description: 测试业务层接口, 用来测试多数据源功能以及其它功能
 * @version: JDK 1.8
 */
public interface TestService
{
    /**
     * @author: Ares
     * @description: 动态数据源测试
     * @date: 2020/3/24 10:06
     * @param: [] 请求参数
     * @return: int 响应参数
     */
    void testDynamicDataSource();

    /**
     * @author: Ares
     * @description: 测试插入
     * @date: 2020/3/26 10:14
     * @param: [] 请求参数
     * @return: int 响应参数
     */
    void testInsertPayment();

    void testDynamicDataSourceTransactional();

    void testDynamicDataSourceTransactionalUseMethod();
}
