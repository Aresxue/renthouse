package com.asiainfo.strategy.mapper.datasourceOne;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * @author: Ares
 * @date: 2020/4/23 14:25
 * @description: 测试mapper, 用来测试不同mapper不同数据源
 * @version: JDK 1.8
 */
@Mapper
@Repository
public interface TestMapperOne
{
    /**
     * @author: Ares
     * @description: 动态数据源测试
     * @date: 2020/3/23 15:02
     * @param: [] 请求参数
     * @return: int 响应参数
     */
    int testDynamicDataSource();
}
