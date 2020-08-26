package com.asiainfo.frame.base;

import java.io.Serializable;

/**
 * @author: Ares
 * @date: 2019/6/11 17:39
 * @description: 响应基类
 * @version: JDK 1.8
 */
public class BaseResponse<T> implements Serializable
{
    private static final long serialVersionUID = -6202397032693082979L;

    /**
     * 响应码
     */
    private String responseCode;
    /**
     * 响应描述
     */
    private String responseDesc;
    /**
     * 响应数据(分页时为分页响应容器否则即为响应数据)
     */
    private T data;

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

    public void setResponseDesc(String responseDesc, Object ... params)
    {
        this.responseDesc = String.format(responseDesc, params);
    }

    public T getData()
    {
        return data;
    }

    public void setData(T data)
    {
        this.data = data;
    }

    public BaseResponse()
    {

    }

    public BaseResponse(ResponseInfo responseInfo)
    {
        this.responseCode = responseInfo.getResponseCode();
        this.responseDesc = responseInfo.getResponseDesc();
    }

    public void setResponseInfo(ResponseInfo responseInfo)
    {
        this.responseCode = responseInfo.getResponseCode();
        this.responseDesc = responseInfo.getResponseDesc();
    }

    public void setResponseInfo(ResponseInfo responseInfo, Object ... params)
    {
        this.responseCode = responseInfo.getResponseCode();
        this.responseDesc = String.format(responseInfo.getResponseDesc(), params);
    }
    
    @Override
    public String toString()
    {
        return "BaseResponse{" + "responseCode='" + responseCode + '\'' + ", responseDesc='" + responseDesc + '\'' + ", data=" + data + '}';
    }
}
