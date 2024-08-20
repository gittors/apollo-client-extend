package com.gittors.apollo.extend.service;

import com.ctrip.framework.apollo.ConfigChangeListener;
import com.ctrip.framework.apollo.model.ConfigChangeEvent;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author zlliu
 * @date 2024/8/19 0019 17:56
 */
public class CustomiseConfigChangeListener implements ConfigChangeListener {
    // simple config handler
    private List<SimpleConfigListenerHandler> simpleHandlers;

    //  composite config handler
    private List<CompositeConfigListenerHandler> compositeHandlers;

    public CustomiseConfigChangeListener(ConfigurableApplicationContext context) {
        List<SimpleConfigListenerHandler> simpleHandlers = Arrays.stream(context.getBeanNamesForType(SimpleConfigListenerHandler.class))
                .map(beanName -> context.getBean(beanName, SimpleConfigListenerHandler.class))
                .sorted(AnnotationAwareOrderComparator.INSTANCE).collect(Collectors.toList());
        List<CompositeConfigListenerHandler> compositeHandlers = Arrays.stream(context.getBeanNamesForType(CompositeConfigListenerHandler.class))
                .map(beanName -> context.getBean(beanName, CompositeConfigListenerHandler.class))
                .sorted(AnnotationAwareOrderComparator.INSTANCE).collect(Collectors.toList());
        this.simpleHandlers = Collections.unmodifiableList(simpleHandlers);
        this.compositeHandlers = Collections.unmodifiableList(compositeHandlers);
    }

    @Override
    public void onChange(ConfigChangeEvent changeEvent) {
        //  step 1: 一对一的回调
        for (String key : changeEvent.changedKeys()) {
            for (SimpleConfigListenerHandler handler : simpleHandlers) {
                //  匹配监听KEY
                if (handler.match(key)) {
                    handler.handle(changeEvent);
                }
            }
        }
        //  step 2: 多对一的回调(比如服务网关,多个配置只需要回调一次的情况)
        for (CompositeConfigListenerHandler handler : compositeHandlers) {
            if (handler.match(changeEvent.changedKeys())) {
                handler.handle(changeEvent);
            }
        }
    }
}
