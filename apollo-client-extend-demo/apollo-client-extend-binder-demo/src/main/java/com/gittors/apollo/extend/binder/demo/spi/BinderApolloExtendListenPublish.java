package com.gittors.apollo.extend.binder.demo.spi;

import com.gittors.apollo.extend.common.event.BinderRefreshBinderEvent;
import com.gittors.apollo.extend.event.EventPublisher;
import com.gittors.apollo.extend.spi.ApolloExtendListenPublish;
import com.google.common.collect.Maps;
import org.springframework.beans.factory.BeanFactory;

import java.util.Map;
import java.util.Properties;

/**
 * @author zlliu
 * @date 2022/8/23 11:18
 */
public class BinderApolloExtendListenPublish implements ApolloExtendListenPublish<Properties> {

    private int order = 0;

    @Override
    public void doPublish(BeanFactory beanFactory, Properties data) {
        Map<String, String> configMap = Maps.newHashMap();
        data.stringPropertyNames().stream()
                .forEach(key -> configMap.put(key, data.getProperty(key, "")));

        BinderRefreshBinderEvent binderEvent = BinderRefreshBinderEvent.getInstance();
        binderEvent.setData(configMap);
        binderEvent.setSource("BinderApolloExtendListenPublish#doPublish");
        EventPublisher eventPublisher = beanFactory.getBean(EventPublisher.class);
        eventPublisher.asyncPublish(binderEvent);
    }

    @Override
    public int getOrder() {
        return order;
    }
}
