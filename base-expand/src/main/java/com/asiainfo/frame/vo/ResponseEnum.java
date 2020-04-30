package com.asiainfo.frame.vo;

/**
 * @author: Ares
 * @date: 2019/6/13 12:05
 * @description: 响应信息
 * @version: JDK 1.8
 */
public enum ResponseEnum implements ResponseEnumInfc
{
    /**
     * 0开头调用成功
     */
    SUCCESS("0000", "操作成功", ""),
    /**
     * 1开头远程调用失败
     */
    INVOKE_FAILURE("1000", "远程调用失败", "远程调用失败: "),
    INVOKE_FAILURE_NOT_FOUND_METHOD("1001", "找不到对应的方法, 请检查调用服务是否配置AresProvider注解并检查启动日志", "找不到对应的方法: {}, 请检查调用服务是否配置AresProvider注解并检查启动日志"),
    INVOKE_FAILURE_MORE_THAN_ONE("1002", "发现多个同组别版本服务实现, 请指定不同版本或修改接口", "发现多个同组别版本服务实现: {}, 请指定不同版本或修改接口"),
    INVOKE_FAILURE_JSON_PARSE("1003", "解析Json参数失败", "解析Json参数: {}失败"),
    INVOKE_FAILURE_DATE_ERROR("1004", "参数为字符串而不是日期", "参数: {}为字符串而不是日期"),
    INVOKE_FAILURE_NOT_FOUND_CLASS("1005", "类不存在","类: {}不存在"),
    INVOKE_FAILURE_NOT_FOUND_LOCAL_METHOD("1006", "方法不存在","方法: {}不存在"),
    INVOKE_FAILURE_NOT_FOUND_SERVICE("1007", "服务不存在","服务: {}不存在"),
    /**
     * 9代表未知
     */
    UNKNOWN_THROWABLE("9000", "未知状况", "未知状况: "),
    UNKNOWN_EXCEPTION("9001", "未知异常", "未知异常: "),
    UNKNOWN_ERROR("9002", "未知错误", "未知未知错误异常: ");

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

    ResponseEnum(String responseCode, String responseDesc, String loggerDesc)
    {
        this.responseCode = responseCode;
        this.responseDesc = responseDesc;
        this.loggerDesc = loggerDesc;
    }

    @Override
    public String getResponseCode()
    {
        return this.responseCode;
    }

    @Override
    public String getResponseDesc()
    {
        return this.responseDesc;
    }

    public String getLoggerDesc()
    {
        return loggerDesc;
    }
}
