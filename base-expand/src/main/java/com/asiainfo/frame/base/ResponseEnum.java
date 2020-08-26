package com.asiainfo.frame.base;

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
    SUCCESS("0000", "操作成功"),
    /**
     * 1开头远程调用失败
     */
    INVOKE_FAILURE("1000", "远程调用失败"), INVOKE_FAILURE_NOT_FOUND_SERVICE("1001", "在当前实例找不到对应的服务,请检查调用服务是否配置RemoteService注解"), INVOKE_FAILURE_MORE_THAN_ONE("1002", "发现多个同版本服务实现,请指定不同版本或修改接口"), INVOKE_FAILURE_JSON_PARSE("1003", "解析Json参数失败"), INVOKE_FAILURE_DATE_ERROR("1004", "该参数为字符串而不是日期"), INVOKE_FAILURE_DEAL_PACKAGE("1005", "处理包装类数据发生异常"),
    /**
     * 9代表未知错误
     */
    UNKNOWN_ERROR("9000", "未知错误"), UNKNOWN_EXCEPTION("9001", "未知异常");

    /**
     * 响应码
     */
    private final String responseCode;
    /**
     * 响应描述
     */
    private final String responseDesc;

    ResponseEnum(String responseCode, String responseDesc)
    {
        this.responseCode = responseCode;
        this.responseDesc = responseDesc;
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
}
