package com.asiainfo.frame.base;

/**
 * @author: Ares
 * @date: 2020/8/26 18:30
 * @description: 业务响应信息统一管理
 * @version: JDK 1.8
 */
public enum ResponseInfo
{
    /**
     * 0开头调用成功
     */
    SUCCESS("000000", "操作成功", ""),
    /**
     * 9代表未知
     */
    UNKNOWN_THROWABLE("900000", "未知状况", "未知状况: "),
    UNKNOWN_EXCEPTION("900001", "未知异常", "未知异常: "),
    UNKNOWN_ERROR("900002", "未知错误", "未知未知错误异常: ");

    /**
     * 响应码
     */
    private final String responseCode;
    /**
     * 响应描述
     */
    private final String responseDesc;
    /**
     * 日志输出
     */
    private final String loggerDesc;

    ResponseInfo(String responseCode, String responseDesc, String loggerDesc)
    {
        this.responseCode = responseCode;
        this.responseDesc = responseDesc;
        this.loggerDesc = loggerDesc;
    }

    public String getResponseCode()
    {
        return this.responseCode;
    }

    public String getResponseDesc()
    {
        return this.responseDesc;
    }

    public String getLoggerDesc()
    {
        return loggerDesc;
    }
}
