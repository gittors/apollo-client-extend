package com.gittors.apollo.extend.binder.demo.spi;

import com.gittors.apollo.extend.common.event.BinderRefreshBinderEvent;
import com.gittors.apollo.extend.event.EventPublisher;
import com.gittors.apollo.extend.spi.ApolloExtendListenPublish;
import org.springframework.beans.factory.BeanFactory;

import java.util.Map;

/**
 * @author zlliu
 * @date 2022/8/23 11:18
 */
public class BinderApolloExtendListenPublish implements ApolloExtendListenPublish<Map<String, Map<String, String>>> {

    private int order = 0;

    @Override
    public void doPublish(BeanFactory beanFactory, Map<String, Map<String, String>> data) {
        BinderRefreshBinderEvent binderEvent = BinderRefreshBinderEvent.getInstance();
        binderEvent.setData(data);
        binderEvent.setSource("BinderApolloExtendListenPublish#doPublish");
        EventPublisher eventPublisher = beanFactory.getBean(EventPublisher.class);
        eventPublisher.asyncPublish(binderEvent);
    }

    @Override
    public int getOrder() {
        return order;
    }
}
