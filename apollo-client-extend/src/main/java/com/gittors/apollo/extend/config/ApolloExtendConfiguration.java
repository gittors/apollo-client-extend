package com.gittors.apollo.extend.config;

import com.gittors.apollo.extend.callback.ApolloExtendCallback;
import com.gittors.apollo.extend.common.constant.CommonApolloConstant;
import com.gittors.apollo.extend.common.enums.ChangeType;
import com.gittors.apollo.extend.initializer.ApolloExtendCustomListenerInitializer;
import com.gittors.apollo.extend.service.ApolloExtendCallbackAdapter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
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
    @ConditionalOnMissingBean(
            name = CommonApolloConstant.DEFAULT_APOLLO_EXTEND_CALLBACK_ADAPTER
    )
    public ApolloExtendCallback defaultApolloExtendCallbackAdapter() {
        return new ApolloExtendCallbackAdapter() {
            @Override
            protected void changeProcess(ChangeType changeType, Map<String, List<Map<String, String>>> data) {
            }
        };
    }

}
