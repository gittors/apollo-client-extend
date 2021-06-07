package com.gittors.apollo.extend.event;

import com.nepxion.eventbus.core.EventControllerFactory;

/**
 * @author zlliu
 * @date 2020/7/8 11:29
 */
public class ExtendEventPublisher implements EventPublisher<Object> {
    public static final String BEAN_NAME = "EventBusPublisher";

    private EventControllerFactory eventControllerFactory;

    public ExtendEventPublisher(EventControllerFactory eventControllerFactory) {
        this.eventControllerFactory = eventControllerFactory;
    }

    @Override
    public void asyncPublish(Object object) {
        eventControllerFactory.getAsyncController().post(object);
    }

    @Override
    public void syncPublish(Object object) {
        eventControllerFactory.getSyncController().post(object);
    }

}
