package com.gittors.apollo.extend.admin.web.spi;

import com.ctrip.framework.apollo.core.spi.Ordered;
import com.gittors.apollo.extend.binder.event.BinderRefreshBinderEvent;
import com.gittors.apollo.extend.event.EventPublisher;
import org.springframework.beans.factory.BeanFactory;

import java.util.Map;

/**
 * @author zlliu
 * @date 2020/8/30 17:43
 */
public class DefaultApolloExtendAdminProcessor implements ApolloExtendAdminProcessor<BeanFactory> {
    @Override
    public void process(BeanFactory request, Object... objects) {
        pushBinder(request, (Map<String, Map<String, String>>) objects[0]);
    }

    /**
     * 推送绑定事件
     * @param beanFactory
     * @param config
     */
    protected void pushBinder(BeanFactory beanFactory, Map<String, Map<String, String>> config) {
        EventPublisher eventPublisher = beanFactory.getBean(EventPublisher.class);
        eventPublisher.asyncPublish(new BinderRefreshBinderEvent(config));
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
