package com.asiainfo.frame.base;

import java.io.Serializable;

/**
 * @author: Ares
 * @date: 2019/6/11 17:39
 * @description: 响应基类
 * @version: JDK 1.8
 */
public class CommonResponse<T> implements Serializable
{
    private static final long serialVersionUID = -6202397032693082979L;

    /**
     * 响应码
     */
    private String code;
    /**
     * 响应信息
     */
    private String message;
    /**
     * 响应数据(分页时为分页响应容器否则即为响应数据)
     */
    private T data;

    public String getCode()
    {
        return code;
    }

    public void setCode(String code)
    {
        this.code = code;
    }

    public String getMessage()
    {
        return message;
    }

    public void setMessage(String message)
    {
        this.message = message;
    }

    public void setResponseDesc(String responseDesc, Object ... params)
    {
        this.message = String.format(responseDesc, params);
    }

    public T getData()
    {
        return data;
    }

    public void setData(T data)
    {
        this.data = data;
    }

    public CommonResponse()
    {

    }

    public CommonResponse(ResponseInfo responseInfo)
    {
        this.code = responseInfo.getResponseCode();
        this.message = responseInfo.getResponseDesc();
    }

    public void setResponseInfo(ResponseInfo responseInfo)
    {
        this.code = responseInfo.getResponseCode();
        this.message = responseInfo.getResponseDesc();
    }

    public void setResponseInfo(ResponseInfo responseInfo, Object ... params)
    {
        this.code = responseInfo.getResponseCode();
        this.message = String.format(responseInfo.getResponseDesc(), params);
    }
    
    @Override
    public String toString()
    {
        return "BaseResponse{" + "responseCode='" + code + '\'' + ", responseDesc='" + message
            + '\'' + ", data=" + data + '}';
    }
}
