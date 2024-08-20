package com.gittors.apollo.extend.binder.demo.event;

import com.gittors.apollo.extend.common.event.BinderRefreshEvent;
import com.gittors.apollo.extend.event.EventPublisher;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Map;

/**
 * @author zlliu
 * @date 2024/8/19 0019 12:22
 */
public class EventPublishDelegate {

    /**
     * 发送更新事件
     * @param context
     * @param data
     * @param msg
     */
    public static void publish(ConfigurableApplicationContext context, Map<String, Map<String, String>> data, String msg) {
        BinderRefreshEvent binderEvent = BinderRefreshEvent.getInstance();
        binderEvent.setData(data);
        binderEvent.setSource(msg);

        EventPublisher eventPublisher = context.getBean(EventPublisher.class);
        eventPublisher.asyncPublish(binderEvent);
    }
}
