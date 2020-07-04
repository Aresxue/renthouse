package com.asiainfo.strategy.config;

import com.asiainfo.strategy.service.ConditionService;
import com.asiainfo.strategy.service.impl.ConditionServiceCloudImpl;
import com.asiainfo.strategy.service.impl.ConditionServiceLocalImpl;
import com.asiainfo.strategy.service.impl.ConditionServiceRemoteImpl;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;

/**
 * @author: Ares
 * @date: 2020/6/4 12:04
 * @description: 根据配置选取不同配置类
 * @version: JDK 1.8
 */
@SpringBootConfiguration
public class ConditionConfig
{
    @Bean
    @Conditional(ConfigCondition.LocalCondition.class)
    public ConditionService localConfig()
    {
        return new ConditionServiceLocalImpl();
    }

    @Bean
    @Conditional({ConfigCondition.RemoteCondition.class})
    public ConditionService remoteConfig()
    {
        return new ConditionServiceRemoteImpl();
    }

    @Bean
    @Conditional({ConfigCondition.CloudCondition.class})
    public ConditionService cloudCondition()
    {
        return new ConditionServiceCloudImpl();
    }
}
