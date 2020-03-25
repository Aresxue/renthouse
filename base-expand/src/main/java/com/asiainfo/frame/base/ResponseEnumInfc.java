package com.asiainfo.frame.base;

/**
 * @author: Ares
 * @date: 2019/6/13 12:04
 * @description:
 * @version: JDK 1.8
 */
public interface ResponseEnumInfc
{
    /**
     * @author: Ares
     * @description: 获取响应码
     * @date: 2019/6/13 12:04
     * @Param: [] 请求参数
     * @return: java.lang.String 响应参数
     */
    String getResponseCode();

    /**
     * @author: Ares
     * @description: 获取响应描述
     * @date: 2019/6/13 12:05
     * @Param: [] 请求参数
     * @return: java.lang.String 响应参数
     */
    String getResponseDesc();
}
