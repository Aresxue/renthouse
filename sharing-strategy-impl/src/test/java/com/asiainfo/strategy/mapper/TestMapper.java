package com.asiainfo.strategy.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

/**
 * @author: Ares
 * @date: 2020/3/23 14:33
 * @description: 测试Mapper类, 用来测试多数据源功能以及一些sql语句
 * @version: JDK 1.8
 */
@Mapper
@Repository
public interface TestMapper
{
    /**
     * @author: Ares
     * @description: 动态数据源测试
     * @date: 2020/3/23 15:02
     * @param: [] 请求参数
     * @return: int 响应参数
     */
    @Select("select payment_id from payment_202003 limit 1 ")
    int testDynamicDataSource();

    /**
     * @author: Ares
     * @description:
     * @date: 2020/3/26 10:12
     * @param: [value] 请求参数
     * @return: int 响应参数
     */
    int testInsertPayment(@param("value") int value);

}
