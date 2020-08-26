package com.asiainfo.frame.base;

/**
 * @author: Ares
 * @date: 2020/8/26 18:01
 * @description: 分页请求基类
 * @version: JDK 1.8
 */
public class BasePageRequest extends BaseRequest
{
    /**
     * 页号, 从0开始
     */
    private String pageNum;
    /**
     * 单页数据量
     */
    private String pageSize;

    public String getPageNum()
    {
        return pageNum;
    }

    public void setPageNum(String pageNum)
    {
        this.pageNum = pageNum;
    }

    public String getPageSize()
    {
        return pageSize;
    }

    public void setPageSize(String pageSize)
    {
        this.pageSize = pageSize;
    }

    @Override
    public String toString()
    {
        return "BasePageRequest{" + "pageNum='" + pageNum + '\'' + ", pageSize='" + pageSize + '\'' + "} " + super.toString();
    }
}
