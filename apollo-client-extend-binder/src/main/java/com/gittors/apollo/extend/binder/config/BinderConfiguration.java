package com.gittors.apollo.extend.binder.config;

import com.gittors.apollo.extend.binder.event.BinderEventSubscriber;
import com.gittors.apollo.extend.binder.processor.BinderHolderBeanPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author zlliu
 * @date 2020/8/20 13:53
 */
@Configuration(proxyBeanMethods = false)
public class BinderConfiguration {

    @Bean
    @ConditionalOnMissingBean(name = BinderHolderBeanPostProcessor.BEAN_NAME)
    public BinderHolderBeanPostProcessor binderHolderBeanPostProcessor() {
        return new BinderHolderBeanPostProcessor();
    }

    @Bean
    @ConditionalOnMissingBean(name = BinderEventSubscriber.BEAN_NAME)
    public BinderEventSubscriber binderEventSubscriber(ConfigurableApplicationContext context) {
        return new BinderEventSubscriber(context);
    }
}
