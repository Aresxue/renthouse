package com.asiainfo.frame.exceptions;

import com.asiainfo.frame.base.ResponseEnum;

/**
 * @author: Ares
 * @date: 2019/6/10 19:51
 * @description: 远程调用自定义异常
 * @version: JDK 1.8
 */
public class RemoteInvokeException extends RuntimeException
{
    private static final long serialVersionUID = -2038262924659942460L;
    /**
     * 响应码
     */
    private String errCode;
    /**
     * 响应描述
     */
    private String errMsg;

    public RemoteInvokeException()
    {
    }

    public RemoteInvokeException(ResponseEnum responseEnum)
    {
        this.errCode = responseEnum.getResponseCode();
        this.errMsg = responseEnum.getResponseDesc();
    }

    public RemoteInvokeException(String errCode, String errMsg)
    {
        this.errCode = errCode;
        this.errMsg = errMsg;
    }

    public RemoteInvokeException(String message)
    {
        super(message);
    }

    public RemoteInvokeException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public RemoteInvokeException(Throwable cause)
    {
        super(cause);
    }

    public String getErrCode()
    {
        return errCode;
    }

    public void setErrCode(String errCode)
    {
        this.errCode = errCode;
    }

    public String getErrMsg()
    {
        return errMsg;
    }

    public void setErrMsg(String errMsg)
    {
        this.errMsg = errMsg;
    }
}
