package com.asiainfo.strategy.aop;

import org.aspectj.lang.annotation.Pointcut;

/**
 * @author Ares
 * @version JDK 1.8
 * @date 2018/12/5 17:33
 * @description: 切点管理公共类
 * 集中管理切点定义
 */
public class CommonJoinPointConfig
{
    /**
     * @author: Ares
     * @description: 将函数入参出参统一Json化打印到控制台
     * @date: 2019/8/13 15:15
     * @param: 请求参数
     * @return: 响应参数
     */
    @Pointcut("(execution(* com.asiainfo.strategy.service.impl.*.*(..))) || (execution(* com.asiainfo.strategy.business.impl.*.*(..)))")
    public void printParamByJson()
    {
    }
}
