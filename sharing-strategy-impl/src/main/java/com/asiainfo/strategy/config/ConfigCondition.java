package com.asiainfo.strategy.config;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.lang.Nullable;

/**
 * @author: Ares
 * @date: 2020/6/4 12:06
 * @description: 配置条件
 * @version: JDK 1.8
 */
public class ConfigCondition
{
    static class LocalCondition implements Condition
    {
        @Override
        public boolean matches(@Nullable ConditionContext conditionContext, @Nullable AnnotatedTypeMetadata annotatedTypeMetadata)
        {
            String name = null;
            if (null != conditionContext)
            {
                name = conditionContext.getEnvironment().getProperty("config.type");
            }
            return "local".equals(name);
        }
    }

    static class RemoteCondition implements Condition
    {
        @Override
        public boolean matches(@Nullable ConditionContext conditionContext, @Nullable AnnotatedTypeMetadata annotatedTypeMetadata)
        {
            String name = null;
            if (null != conditionContext)
            {
                name = conditionContext.getEnvironment().getProperty("config.type");
            }
            return "remote".equals(name);
        }
    }

    static class CloudCondition implements Condition
    {
        @Override
        public boolean matches(@Nullable ConditionContext conditionContext, @Nullable AnnotatedTypeMetadata annotatedTypeMetadata)
        {
            String name = null;
            if (null != conditionContext)
            {
                name = conditionContext.getEnvironment().getProperty("config.type");
            }
            return "cloud".equals(name);
        }
    }

}
