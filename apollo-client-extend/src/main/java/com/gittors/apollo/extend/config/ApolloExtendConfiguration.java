package com.gittors.apollo.extend.config;

import com.gittors.apollo.extend.callback.ApolloExtendCallback;
import com.gittors.apollo.extend.common.constant.CommonApolloConstant;
import com.gittors.apollo.extend.common.constant.CommonSwitchConstant;
import com.gittors.apollo.extend.processor.AutoUpdateConfigListenerProcessor;
import com.gittors.apollo.extend.service.ApolloExtendAddListenCallback;
import com.gittors.apollo.extend.service.ApolloExtendCallbackAdapter;
import com.gittors.apollo.extend.service.ApolloExtendDeleteListenCallback;
import com.gittors.apollo.extend.service.ApolloExtendGlobalListenCallback;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author zlliu
 * @date 2020/7/25 11:06
 */
@Configuration(proxyBeanMethods = false)
public class ApolloExtendConfiguration {
    @Bean
    public AutoUpdateConfigListenerProcessor updateConfigListenerProcessor() {
        return new AutoUpdateConfigListenerProcessor();
    }

    @Bean(CommonApolloConstant.DEFAULT_APOLLO_EXTEND_CALLBACK_ADAPTER)
    @ConditionalOnMissingBean(name = CommonApolloConstant.DEFAULT_APOLLO_EXTEND_CALLBACK_ADAPTER)
    public ApolloExtendCallback defaultApolloExtendCallbackAdapter() {
        return new ApolloExtendCallbackAdapter();
    }

    @Bean(ApolloExtendAddListenCallback.BEAN_NAME)
    @ConditionalOnMissingBean(name = ApolloExtendAddListenCallback.BEAN_NAME)
    @ConditionalOnProperty(
            name = CommonSwitchConstant.APOLLO_EXTEND_ADD_LISTEN_CALL,
            matchIfMissing = true,
            havingValue = "true"
    )
    public ApolloExtendCallback apolloExtendAddCallback(ConfigurableApplicationContext context) {
        return new ApolloExtendAddListenCallback(context);
    }

    @Bean(ApolloExtendDeleteListenCallback.BEAN_NAME)
    @ConditionalOnMissingBean(name = ApolloExtendDeleteListenCallback.BEAN_NAME)
    @ConditionalOnProperty(
            name = CommonSwitchConstant.APOLLO_EXTEND_DELETE_LISTEN_CALL,
            matchIfMissing = true,
            havingValue = "true"
    )
    public ApolloExtendCallback apolloExtendDeleteCallback(ConfigurableApplicationContext context) {
        return new ApolloExtendDeleteListenCallback(context);
    }

    @Bean(ApolloExtendGlobalListenCallback.BEAN_NAME)
    @ConditionalOnMissingBean(name = ApolloExtendGlobalListenCallback.BEAN_NAME)
    @ConditionalOnProperty(
            name = CommonSwitchConstant.APOLLO_EXTEND_GLOBAL_LISTEN_CALL,
            matchIfMissing = true,
            havingValue = "true"
    )
    public ApolloExtendCallback apolloExtendGlobalListenCallback(ConfigurableApplicationContext context) {
        return new ApolloExtendGlobalListenCallback(context);
    }

}
