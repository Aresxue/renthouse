package com.asiainfo.frame.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;

/**
 * @author Ares
 * @version JDK 1.8
 * @date 2018/12/24 15:58
 * @description: IO操作流通用的操作类
 */
public class IOHandleUtil
{
    private static final Logger LOGGER = LoggerFactory.getLogger(IOHandleUtil.class);

    /**
     * @author: Ares
     * @description: 关闭流
     * @date: 2019/9/29 17:36
     * @param: [closeable] 请求参数
     * @return: void 响应参数
     */
    public static void closeIOSteam(AutoCloseable closeable)
    {
        if (null != closeable)
        {
            try
            {
                closeable.close();
            } catch (SQLException e)
            {
                LOGGER.error("关闭sql相关连接或对象时失败: ", e);
            } catch (IOException e)
            {
                LOGGER.error("关闭IO操作流时失败: ", e);
            } catch (Exception e)
            {
                LOGGER.error("IO通用关闭方法调用出错: ", e);
            }
        }
    }
}
