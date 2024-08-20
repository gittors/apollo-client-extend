package com.gittors.apollo.extend.config;

import com.gittors.apollo.extend.common.constant.CommonSwitchConstant;
import com.gittors.apollo.extend.processor.ApolloConfigListenerProcessor;
import com.gittors.apollo.extend.service.ApolloExtendAddListenCallback;
import com.gittors.apollo.extend.service.ApolloExtendCallbackAdapter;
import com.gittors.apollo.extend.service.ApolloExtendDeleteListenCallback;
import com.gittors.apollo.extend.service.ApolloExtendGlobalListenCallback;
import com.gittors.apollo.extend.service.CustomiseConfigChangeListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author zlliu
 * @date 2020/7/25 11:06
 */
@Configuration
public class ApolloExtendConfiguration {

    @Bean
    @ConditionalOnMissingBean(CustomiseConfigChangeListener.class)
    public CustomiseConfigChangeListener managerConfigChangeListener(ConfigurableApplicationContext context) {
        return new CustomiseConfigChangeListener(context);
    }

    @Bean
    @ConditionalOnMissingBean(ApolloConfigListenerProcessor.class)
    public ApolloConfigListenerProcessor updateConfigListenerProcessor() {
        return new ApolloConfigListenerProcessor();
    }

    @Bean
    @ConditionalOnMissingBean(ApolloExtendCallbackAdapter.class)
    public ApolloExtendCallbackAdapter apolloExtendCallbackAdapter() {
        return new ApolloExtendCallbackAdapter();
    }

    @Bean(ApolloExtendAddListenCallback.BEAN_NAME)
    @ConditionalOnMissingBean(name = ApolloExtendAddListenCallback.BEAN_NAME)
    @ConditionalOnProperty(
            name = CommonSwitchConstant.APOLLO_EXTEND_ADD_LISTEN_CALL,
            matchIfMissing = true,
            havingValue = "true"
    )
    public ApolloExtendAddListenCallback apolloExtendAddCallback(ConfigurableApplicationContext context) {
        return new ApolloExtendAddListenCallback(context);
    }

    @Bean(ApolloExtendDeleteListenCallback.BEAN_NAME)
    @ConditionalOnMissingBean(name = ApolloExtendDeleteListenCallback.BEAN_NAME)
    @ConditionalOnProperty(
            name = CommonSwitchConstant.APOLLO_EXTEND_DELETE_LISTEN_CALL,
            havingValue = "true"
    )
    public ApolloExtendDeleteListenCallback apolloExtendDeleteCallback(ConfigurableApplicationContext context) {
        return new ApolloExtendDeleteListenCallback(context);
    }

    @Bean(ApolloExtendGlobalListenCallback.BEAN_NAME)
    @ConditionalOnMissingBean(name = ApolloExtendGlobalListenCallback.BEAN_NAME)
    @ConditionalOnProperty(
            name = CommonSwitchConstant.APOLLO_EXTEND_GLOBAL_LISTEN_CALL,
            havingValue = "true"
    )
    public ApolloExtendGlobalListenCallback apolloExtendGlobalListenCallback(ConfigurableApplicationContext context) {
        return new ApolloExtendGlobalListenCallback(context);
    }

}
