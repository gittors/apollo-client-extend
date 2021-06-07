package com.gittors.apollo.extend.event.config;

import com.gittors.apollo.extend.event.EventPublisher;
import com.gittors.apollo.extend.event.ExtendEventPublisher;
import com.nepxion.eventbus.annotation.EnableEventBus;
import com.nepxion.eventbus.configuration.EventConfiguration;
import com.nepxion.eventbus.core.EventControllerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author zlliu
 * @date 2020/7/8 11:29
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(
        name = "event.bus.enable",
        havingValue = "true",
        matchIfMissing = true
)
@ConditionalOnClass(EventConfiguration.class)
@EnableEventBus
public class ApolloExtendEventConfiguration {

    @Bean(ExtendEventPublisher.BEAN_NAME)
    @ConditionalOnBean(name = "eventControllerFactory")
    @ConditionalOnMissingBean(EventPublisher.class)
    public EventPublisher eventBusPublisher(ObjectProvider<EventControllerFactory> controllerFactory) {
        return new ExtendEventPublisher(controllerFactory.getIfAvailable());
    }

}
