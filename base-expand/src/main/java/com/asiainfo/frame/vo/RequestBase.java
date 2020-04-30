package com.asiainfo.frame.vo;

/**
 * @author: Ares
 * @date: 2019/6/11 17:37
 * @description: 请求基类
 * @version: JDK 1.8
 */
public class RequestBase
{
    private String userName;
    private String password;

    public String getUserName()
    {
        return userName;
    }

    public void setUserName(String userName)
    {
        this.userName = userName;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    @Override
    public String toString()
    {
        return "RequestBase{" + "userName='" + userName + '\'' + ", password='" + password + '\'' + '}';
    }
}
