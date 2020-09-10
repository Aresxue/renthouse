package com.asiainfo.frame.base;

/**
 * @author: Ares
 * @date: 2020/8/26 18:01
 * @description: 分页请求基类
 * @version: JDK 1.8
 */
public class BasePageRequest extends BaseRequest
{
    private static final long serialVersionUID = 7504999184499520486L;

    /**
     * 页号, 从0开始
     */
    private Long pageNum;
    /**
     * 单页数据量
     */
    private Long pageSize;

    public Long getPageNum()
    {
        return pageNum;
    }

    public void setPageNum(Long pageNum)
    {
        this.pageNum = pageNum;
    }

    public Long getPageSize()
    {
        return pageSize;
    }

    public void setPageSize(Long pageSize)
    {
        this.pageSize = pageSize;
    }

    @Override
    public String toString()
    {
        return "BasePageRequest{" + "pageNum='" + pageNum + '\'' + ", pageSize='" + pageSize + '\'' + "} " + super.toString();
    }
}
