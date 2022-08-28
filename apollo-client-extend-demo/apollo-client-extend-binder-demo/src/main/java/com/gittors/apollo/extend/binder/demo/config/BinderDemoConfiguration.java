package com.gittors.apollo.extend.binder.demo.config;

import com.gittors.apollo.extend.binder.demo.service.BinderDemoCallback;
import com.gittors.apollo.extend.callback.ApolloExtendCallback;
import com.gittors.apollo.extend.common.constant.CommonApolloConstant;
import com.gittors.apollo.extend.config.ApolloExtendConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author zlliu
 * @date 2020/8/21 20:41
 */
@Configuration(proxyBeanMethods = false)
@AutoConfigureBefore(ApolloExtendConfiguration.class)
public class BinderDemoConfiguration {

    @Bean(CommonApolloConstant.DEFAULT_APOLLO_EXTEND_CALLBACK_ADAPTER)
    @ConditionalOnMissingBean(
            name = CommonApolloConstant.DEFAULT_APOLLO_EXTEND_CALLBACK_ADAPTER
    )
    public ApolloExtendCallback apolloCallback(ConfigurableApplicationContext context) {
        return new BinderDemoCallback(context);
    }
}
