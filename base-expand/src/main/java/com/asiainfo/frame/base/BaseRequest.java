package com.asiainfo.frame.base;

import java.io.Serializable;
import java.util.List;

/**
 * @author: Ares
 * @date: 2019/6/11 17:37
 * @description: 请求基类
 * @version: JDK 1.8
 */
public class BaseRequest implements Serializable
{
    private static final long serialVersionUID = 8776458218925079527L;

    /**
     * 用户标识
     */
    private Long userId;
    /**
     * 用户编号(员工号)
     */
    private String userNumber;
    /**
     * 用户名称
     */
    private String userName;
    /**
     * 角色列表, 一个用户可有多个角色，每个角色对应多种权限
     */
    private List<Long> roleList;
    /**
     * 组织机构标识, 一个用户只会归属于一个组织, 组织下会有多种权限
     */
    private Long orgId;

    public Long getUserId()
    {
        return userId;
    }

    public void setUserId(Long userId)
    {
        this.userId = userId;
    }

    public String getUserNumber()
    {
        return userNumber;
    }

    public void setUserNumber(String userNumber)
    {
        this.userNumber = userNumber;
    }

    public String getUserName()
    {
        return userName;
    }

    public void setUserName(String userName)
    {
        this.userName = userName;
    }

    public List<Long> getRoleList()
    {
        return roleList;
    }

    public void setRoleList(List<Long> roleList)
    {
        this.roleList = roleList;
    }

    public Long getOrgId()
    {
        return orgId;
    }

    public void setOrgId(Long orgId)
    {
        this.orgId = orgId;
    }

    @Override
    public String toString()
    {
        return "BaseRequest{" + "userId=" + userId + ", userNumber='" + userNumber + '\'' + ", userName='" + userName + '\'' + ", roleList=" + roleList + ", orgId=" + orgId + '}';
    }
}
