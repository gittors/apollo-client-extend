package com.gittors.apollo.extend.binder.demo.event;

import com.gittors.apollo.extend.common.event.BinderRefreshEvent;
import com.gittors.apollo.extend.event.EventPublisher;
import org.springframework.beans.factory.BeanFactory;

import java.util.Map;

/**
 * @author zlliu
 * @date 2024/8/19 0019 12:22
 */
public class EventPublishDelegate {

    /**
     * 发送更新事件
     * @param beanFactory
     * @param data
     * @param msg
     */
    public static void publish(BeanFactory beanFactory, Map<String, Map<String, String>> data, String msg) {
        BinderRefreshEvent binderEvent = BinderRefreshEvent.getInstance();
        binderEvent.setData(data);
        binderEvent.setSource(msg);

        EventPublisher eventPublisher = beanFactory.getBean(EventPublisher.class);
        eventPublisher.asyncPublish(binderEvent);
    }
}
