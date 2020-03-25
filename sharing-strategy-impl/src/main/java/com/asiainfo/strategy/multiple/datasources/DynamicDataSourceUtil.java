package com.asiainfo.strategy.multiple.datasources;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author: Ares
 * @date: 2020/3/19 11:39
 * @description: 动态数据工具类
 * @version: JDK 1.8
 */
public class DynamicDataSourceUtil
{
    private static final Logger LOGGER = LoggerFactory.getLogger(DynamicDataSourceUtil.class);

    /**
     * @author: Ares
     * @description: 根据传入的数据源标识切换数据源
     * @date: 2020/3/20 10:07
     * @param: [datasourceId]
     * 数据源标识
     * @return: void 响应参数
     */
    public static void changeDataSource(String dataSourceId)
    {
        // 调用该方法的上层类的全名
        String className = Thread.currentThread().getStackTrace()[2].getClassName();
        // 调用该方法的上层方法名称
        String invokeMethodName = Thread.currentThread().getStackTrace()[2].getMethodName();
        changeDataSource(dataSourceId, className + DynamicDataSourceConstans.CLASS_AND_METHOD_CONNECTOR + invokeMethodName);
    }

    /**
     * @author: Ares
     * @description: 清理当前线程的数据源标识
     * @date: 2020/3/20 10:07
     * @param: [] 请求参数
     * @return: void 响应参数
     */
    public static void clearDataSourceId()
    {
        DynamicDataSourceContextHolder.clearDataSourceId();
    }

    /**
     * @author: Ares
     * @description: 根据传入的数据源标识切换数据源
     * @date: 2020/3/20 10:07
     * @param: [datasourceId, methodName]
     * 数据源标识, 方法名
     * @return: void 响应参数
     */
    public static void changeDataSource(String dataSourceId, String methodName)
    {
        if (!DynamicDataSourceContextHolder.containsDataSource(dataSourceId))
        {
            LOGGER.error("数据源: {}不存在，方法: {}使用默认数据源", dataSourceId, methodName);
        }
        else
        {
            LOGGER.debug("方法: {}切换至指定数据库: {}", methodName, dataSourceId);
            DynamicDataSourceContextHolder.setDataSourceId(dataSourceId);
        }
    }

}
