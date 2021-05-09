package com.gittors.apollo.extend.config;

import com.gittors.apollo.extend.callback.ApolloExtendCallback;
import com.gittors.apollo.extend.common.constant.CommonApolloConstant;
import com.gittors.apollo.extend.common.constant.CommonSwitchConstant;
import com.gittors.apollo.extend.common.enums.ChangeType;
import com.gittors.apollo.extend.initializer.ApolloExtendCustomListenerInitializer;
import com.gittors.apollo.extend.service.ApolloExtendAddListenCallback;
import com.gittors.apollo.extend.service.ApolloExtendCallbackAdapter;
import com.gittors.apollo.extend.service.ApolloExtendDeleteListenCallback;
import com.gittors.apollo.extend.service.ApolloExtendGlobalListenCallback;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;

import java.util.Map;

/**
 * @author zlliu
 * @date 2020/7/25 11:06
 */
@Configuration(proxyBeanMethods = false)
public class ApolloExtendConfiguration {

    @Bean
    public ApolloExtendCustomListenerInitializer apolloExtendCustomListenerInitializer() {
        return new ApolloExtendCustomListenerInitializer();
    }

    @Bean(CommonApolloConstant.DEFAULT_APOLLO_EXTEND_CALLBACK_ADAPTER)
    @ConditionalOnMissingBean(name = CommonApolloConstant.DEFAULT_APOLLO_EXTEND_CALLBACK_ADAPTER)
    public ApolloExtendCallback defaultApolloExtendCallbackAdapter() {
        return new ApolloExtendCallbackAdapter() {
            @Override
            protected void changeProcess(ChangeType changeType, Map<String, Map<String, Map<String, String>>> data) {
            }
        };
    }

    @Bean(ApolloExtendAddListenCallback.BEAN_NAME)
    @ConditionalOnMissingBean(name = ApolloExtendAddListenCallback.BEAN_NAME)
    @ConditionalOnProperty(
            name = CommonSwitchConstant.APOLLO_EXTEND_ADD_LISTEN_CALL_BACK,
            matchIfMissing = true,
            havingValue = "true"
    )
    public ApolloExtendCallback apolloExtendAddCallback(Environment environment) {
        return new ApolloExtendAddListenCallback((ConfigurableEnvironment) environment);
    }

    @Bean(ApolloExtendDeleteListenCallback.BEAN_NAME)
    @ConditionalOnMissingBean(name = ApolloExtendDeleteListenCallback.BEAN_NAME)
    @ConditionalOnProperty(
            name = CommonSwitchConstant.APOLLO_EXTEND_DELETE_LISTEN_CALL_BACK,
            matchIfMissing = true,
            havingValue = "true"
    )
    public ApolloExtendCallback apolloExtendDeleteCallback(Environment environment) {
        return new ApolloExtendDeleteListenCallback((ConfigurableEnvironment) environment);
    }

    @Bean(ApolloExtendGlobalListenCallback.BEAN_NAME)
    @ConditionalOnMissingBean(name = ApolloExtendGlobalListenCallback.BEAN_NAME)
    @ConditionalOnProperty(
            name = CommonSwitchConstant.APOLLO_EXTEND_GLOBAL_LISTEN_CALL_BACK,
            matchIfMissing = true,
            havingValue = "true"
    )
    public ApolloExtendCallback apolloExtendGlobalListenCallback(Environment environment) {
        return new ApolloExtendGlobalListenCallback((ConfigurableEnvironment) environment);
    }

}
