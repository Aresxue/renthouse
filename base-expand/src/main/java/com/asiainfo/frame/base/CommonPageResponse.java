package com.asiainfo.frame.base;

/**
 * @author: Ares
 * @date: 2020/8/26 18:01
 * @description: 分页响应容器
 * @version: JDK 1.8
 */
public class CommonPageResponse<T>
{
    /**
     * 总记录数
     */
    private Long recordCount;
    /**
     * 总页数
     */
    private Long pageCount;
    /**
     * 业务响应数据
     */
    private T businessData;

    public Long getRecordCount()
    {
        return recordCount;
    }

    public void setRecordCount(Long recordCount)
    {
        this.recordCount = recordCount;
    }

    public Long getPageCount()
    {
        return pageCount;
    }

    public void setPageCount(Long pageCount)
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
