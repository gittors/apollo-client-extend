package com.gittors.apollo.extend.event;

import com.nepxion.eventbus.core.EventController;
import com.nepxion.eventbus.core.EventControllerFactory;
import lombok.extern.slf4j.Slf4j;

/**
 * @author zlliu
 * @date 2020/7/8 11:29
 */
@Slf4j
public class ApolloEventBusPublisher implements EventPublisher<Object> {
    public static final String BEAN_NAME = "ApolloEventBusPublisher";

    private EventControllerFactory eventControllerFactory;

    public ApolloEventBusPublisher(EventControllerFactory eventControllerFactory) {
        this.eventControllerFactory = eventControllerFactory;
    }

    @Override
    public void asyncPublish(Object object) {
        if (eventControllerFactory != null) {
            EventController eventController = eventControllerFactory.getAsyncController();
            if (eventController != null) {
                eventController.post(object);
            } else {
                log.error("#asyncPublish error: eventController is null!");
            }
        } else {
            log.error("#asyncPublish error: eventControllerFactory is null!");
        }
    }

    @Override
    public void syncPublish(Object object) {
        if (eventControllerFactory != null) {
            EventController eventController = eventControllerFactory.getSyncController();
            if (eventController != null) {
                eventController.post(object);
            } else {
                log.error("#syncPublish error: eventController is null!");
            }
        } else {
            log.error("#syncPublish error: eventControllerFactory is null!");
        }
    }

}
