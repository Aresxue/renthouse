package com.asiainfo.frame.base;

/**
 * @author: Ares
 * @date: 2020/8/26 19:04
 * @description: 业务异常基类
 * @version: JDK 1.8
 */
public class BaseException extends RuntimeException
{
    private static final long serialVersionUID = 822603967496303220L;

    /**
     * 响应码
     */
    private String errCode;
    /**
     * 响应描述
     */
    private String errMsg;

    public BaseException(ResponseInfo responseInfo)
    {
        this.errCode = responseInfo.getResponseCode();
        this.errMsg = responseInfo.getResponseDesc();
    }

    public BaseException(ResponseInfo responseInfo, Object ... params)
    {
        this.errCode = responseInfo.getResponseCode();
        this.errMsg = String.format(responseInfo.getResponseDesc(), params);
    }

    public BaseException(String errCode, String errMsg)
    {
        this.errCode = errCode;
        this.errMsg = errMsg;
    }

    public BaseException(String message)
    {
        super(message);
    }

    public BaseException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public BaseException(Throwable cause)
    {
        super(cause);
    }
}
