package com.gittors.apollo.extend.binder.demo.service;

import com.gittors.apollo.extend.common.enums.ChangeType;
import com.gittors.apollo.extend.common.event.BinderRefreshBinderEvent;
import com.gittors.apollo.extend.event.EventPublisher;
import com.gittors.apollo.extend.service.ApolloExtendCallbackAdapter;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.Map;

/**
 * @author zlliu
 * @date 2020/8/19 11:18
 */
@Slf4j
public class BinderDemoCallback extends ApolloExtendCallbackAdapter {
    @Autowired
    private BeanFactory beanFactory;

    public BinderDemoCallback() {
    }

    @Override
    protected void changeProcess(ChangeType changeType, Map<String, Map<String, Map<String, String>>> data) {
        Map<String, String> configMap = Maps.newHashMap();
        data.values()
                .stream()
                .map(Map::values)
                .flatMap(Collection::stream)
                .forEach(map -> configMap.putAll(map));
        if (MapUtils.isEmpty(configMap)) {
            log.warn("#changeProcess configMap is empty!");
            return;
        }
        BinderRefreshBinderEvent binderEvent = BinderRefreshBinderEvent.getInstance();
        binderEvent.setData(configMap);
        EventPublisher eventPublisher = beanFactory.getBean(EventPublisher.class);
        eventPublisher.asyncPublish(binderEvent);
    }

}
