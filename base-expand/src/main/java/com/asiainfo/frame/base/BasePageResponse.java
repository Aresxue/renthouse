package com.asiainfo.frame.base;

/**
 * @author: Ares
 * @date: 2020/8/26 18:01
 * @description: 分页响应容器
 * @version: JDK 1.8
 */
public class BasePageResponse<T>
{
    /**
     * 总记录数
     */
    private String recordCount;
    /**
     * 总页数
     */
    private String pageCount;
    /**
     * 业务响应数据
     */
    private T businessData;

    public String getRecordCount()
    {
        return recordCount;
    }

    public void setRecordCount(String recordCount)
    {
        this.recordCount = recordCount;
    }

    public String getPageCount()
    {
        return pageCount;
    }

    public void setPageCount(String pageCount)
    {
        this.pageCount = pageCount;
    }

    public T getBusinessData()
    {
        return businessData;
    }

    public void setBusinessData(T businessData)
    {
        this.businessData = businessData;
    }

    @Override
    public String toString()
    {
        return "BasePageResponse{" + "recordCount='" + recordCount + '\'' + ", pageCount='" + pageCount + '\'' + ", businessData=" + businessData + '}';
    }
}
