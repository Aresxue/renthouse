package com.asiainfo.frame.base;

/**
 * @author: Ares
 * @date: 2019/6/11 17:39
 * @description: 响应基类
 * @version: JDK 1.8
 */
public class BaseResponse<T>
{
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
    
    @Override
    public String toString()
    {
        return "BaseResponse{" + "responseCode='" + responseCode + '\'' + ", responseDesc='" + responseDesc + '\'' + ", data=" + data + '}';
    }
}
