package com.asiainfo.frame.vo;

import com.asiainfo.frame.constants.ResponseEnum;
import com.asiainfo.frame.exceptions.RemoteInvokeException;

/**
 * @author: Ares
 * @date: 2019/6/11 17:39
 * @description: 响应基类
 * @version: JDK 1.8
 */
public class ResponseBase
{
    /**
     * 响应码
     */
    private String responseCode;
    /**
     * 响应描述
     */
    private String responseDesc;

    public ResponseBase()
    {
    }

    public ResponseBase(ResponseEnum responseEnum)
    {
        this.responseCode = responseEnum.getResponseCode();
        this.responseDesc = responseEnum.getResponseDesc();
    }

    public String getResponseCode()
    {
        return responseCode;
    }

    public void setResponseCode(String responseCode)
    {
        this.responseCode = responseCode;
    }

    public String getResponseDesc()
    {
        return responseDesc;
    }

    public void setResponseDesc(String responseDesc)
    {
        this.responseDesc = responseDesc;
    }

    public void setResponseEnum(ResponseEnum responseEnum)
    {
        this.responseCode = responseEnum.getResponseCode();
        this.responseDesc = responseEnum.getResponseDesc();
    }

    public void setRemoteInvokeException(RemoteInvokeException exception)
    {
        this.responseCode = exception.getErrCode();
        this.responseDesc = exception.getErrMsg();
    }

    @Override
    public String toString()
    {
        return "ResponseBase{" + "responseCode='" + responseCode + '\'' + ", responseDesc='" + responseDesc + '\'' + '}';
    }
}
