package com.gittors.apollo.extend.admin.webflux.spi;

import com.ctrip.framework.apollo.core.spi.Ordered;
import com.gittors.apollo.extend.binder.event.BinderRefreshBinderEvent;
import com.gittors.apollo.extend.event.EventPublisher;
import com.google.common.collect.Maps;
import org.springframework.beans.factory.BeanFactory;

import java.util.List;
import java.util.Map;

/**
 * @author zlliu
 * @date 2020/8/30 17:43
 */
public class DefaultApolloExtendAdminWebfluxProcessor implements ApolloExtendAdminWebfluxProcessor<BeanFactory> {
    @Override
    public void process(BeanFactory request, Object... objects) {
        pushBinder(request, (List<Map<String, String>>) objects[0]);
    }

    /**
     * 推送绑定事件
     * @param beanFactory
     * @param list
     */
    protected void pushBinder(BeanFactory beanFactory, List<Map<String, String>> list) {
        Map<String, List<Map<String, String>>> data = Maps.newHashMap();
        data.put("customer operator", list);

        EventPublisher eventPublisher = beanFactory.getBean(EventPublisher.class);
        eventPublisher.asyncPublish(new BinderRefreshBinderEvent(data));
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
