package com.gittors.apollo.extend.admin.web.spi;

import com.ctrip.framework.apollo.core.spi.Ordered;
import com.gittors.apollo.extend.admin.web.endpoint.AdminNamespaceEndpoint;
import com.gittors.apollo.extend.common.event.BinderRefreshBinderEvent;
import com.gittors.apollo.extend.event.EventPublisher;
import com.google.common.collect.Maps;
import org.springframework.context.ApplicationContext;

import java.util.Map;

/**
 * @author zlliu
 * @date 2020/8/30 17:43
 */
public class DefaultApolloExtendAdminProcessor implements ApolloExtendAdminProcessor<ApplicationContext> {
    @Override
    public void process(ApplicationContext request, Object... objects) {
        pushBinder(request, (Map<String, Map<String, String>>) objects[0], (String) objects[1]);
    }

    /**
     * 推送绑定事件
     * @param beanFactory
     * @param config
     */
    protected void pushBinder(ApplicationContext beanFactory, Map<String, Map<String, String>> config, String source) {
        Map<String, String> data = Maps.newHashMap();
        config.values().forEach(map -> data.putAll(map));

        BinderRefreshBinderEvent binderRefreshBinderEvent = BinderRefreshBinderEvent.getInstance();
        binderRefreshBinderEvent.setData(data);
        binderRefreshBinderEvent.setSource(source);
        EventPublisher eventPublisher = beanFactory.getBean(EventPublisher.class);
        eventPublisher.asyncPublish(binderRefreshBinderEvent);
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
