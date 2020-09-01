package com.gittors.apollo.extend.binder.demo.service;

import com.gittors.apollo.extend.binder.event.BinderRefreshBinderEvent;
import com.gittors.apollo.extend.common.enums.ChangeType;
import com.gittors.apollo.extend.event.EventPublisher;
import com.gittors.apollo.extend.service.ApolloExtendCallbackAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
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
    protected void changeProcess(ChangeType changeType, Map<String, List<Map<String, String>>> data) {
        EventPublisher eventPublisher = beanFactory.getBean(EventPublisher.class);
        eventPublisher.asyncPublish(new BinderRefreshBinderEvent(data));
    }
}
