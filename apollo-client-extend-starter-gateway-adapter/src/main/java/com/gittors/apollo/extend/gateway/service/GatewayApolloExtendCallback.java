package com.gittors.apollo.extend.gateway.service;

import com.gittors.apollo.extend.common.enums.ChangeType;
import com.gittors.apollo.extend.event.EventPublisher;
import com.gittors.apollo.extend.gateway.event.RouteRefreshEvent;
import com.gittors.apollo.extend.service.ApolloExtendCallbackAdapter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author zlliu
 * @date 2020/8/19 11:18
 */
@Slf4j
public class GatewayApolloExtendCallback extends ApolloExtendCallbackAdapter {
    @Autowired
    private BeanFactory beanFactory;

    public GatewayApolloExtendCallback() {
    }

    @Override
    protected void changeProcess(ChangeType changeType, Map<String, Map<String, Map<String, String>>> data) {
        Map<String, String> configMap = new HashMap<>();
        data.values()
                .stream()
                .map(Map::values)
                .flatMap(Collection::stream)
                .forEach(map -> configMap.putAll(map));
        if (MapUtils.isEmpty(configMap)) {
            log.warn("#changeProcess configMap is empty!");
            return;
        }
        EventPublisher eventPublisher = beanFactory.getBean(EventPublisher.class);
        RouteRefreshEvent routeRefreshEvent = RouteRefreshEvent.getInstance();
        routeRefreshEvent.setData(configMap);
        eventPublisher.asyncPublish(routeRefreshEvent);
    }
}
