package com.gittors.apollo.extend.gateway.config;

import com.gittors.apollo.extend.callback.ApolloExtendCallback;
import com.gittors.apollo.extend.common.constant.CommonApolloConstant;
import com.gittors.apollo.extend.config.ApolloExtendConfiguration;
import com.gittors.apollo.extend.gateway.service.GatewayApolloExtendCallback;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author zlliu
 * @date 2020/8/19 11:19
 */
@Configuration(proxyBeanMethods = false)
@AutoConfigureBefore(ApolloExtendConfiguration.class)
public class GatewayConfiguration {

    @Bean(CommonApolloConstant.DEFAULT_APOLLO_EXTEND_CALLBACK_ADAPTER)
    @ConditionalOnMissingBean(name = CommonApolloConstant.DEFAULT_APOLLO_EXTEND_CALLBACK_ADAPTER)
    public ApolloExtendCallback gatewayExtensionCallback(ConfigurableApplicationContext context) {
        return new GatewayApolloExtendCallback(context);
    }

}
