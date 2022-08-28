package com.gittors.apollo.extend.gateway.service;

import com.gittors.apollo.extend.event.EventPublisher;
import com.gittors.apollo.extend.gateway.event.RouteRefreshEvent;
import com.gittors.apollo.extend.service.ApolloExtendCallbackAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Map;

/**
 * @author zlliu
 * @date 2020/8/19 11:18
 */
@Slf4j
public class GatewayApolloExtendCallback extends ApolloExtendCallbackAdapter {
    private ConfigurableListableBeanFactory beanFactory;

    public GatewayApolloExtendCallback(ConfigurableApplicationContext context) {
        this.beanFactory = context.getBeanFactory();
    }

    @Override
    protected void changeProcess(Map<String, Map<String, String>> data) {
        //  网关的路由配置一般是多个，不关心具体的KEY，所以直接发送事件通知即可
        EventPublisher eventPublisher = beanFactory.getBean(EventPublisher.class);
        eventPublisher.asyncPublish(RouteRefreshEvent.getInstance());
    }
}
