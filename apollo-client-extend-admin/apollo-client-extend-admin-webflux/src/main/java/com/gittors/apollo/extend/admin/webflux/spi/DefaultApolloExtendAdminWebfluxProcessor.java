package com.gittors.apollo.extend.admin.webflux.spi;

import com.ctrip.framework.apollo.core.spi.Ordered;
import com.gittors.apollo.extend.common.event.BinderRefreshBinderEvent;
import com.gittors.apollo.extend.event.EventPublisher;
import com.google.common.collect.Maps;
import org.springframework.context.ApplicationContext;

import java.util.Map;

/**
 * @author zlliu
 * @date 2020/8/30 17:43
 */
public class DefaultApolloExtendAdminWebfluxProcessor implements ApolloExtendAdminWebfluxProcessor<ApplicationContext> {
    @Override
    public void process(ApplicationContext request, Object... objects) {
        pushBinder(request, (Map<String, Map<String, String>>) objects[0]);
    }

    /**
     * 推送绑定事件
     * @param context
     * @param config
     */
    protected void pushBinder(ApplicationContext context, Map<String, Map<String, String>> config) {
        Map<String, String> data = Maps.newHashMap();
        config.values().forEach(map -> data.putAll(map));

        BinderRefreshBinderEvent binderRefreshBinderEvent = BinderRefreshBinderEvent.getInstance();
        binderRefreshBinderEvent.setData(data);
        EventPublisher eventPublisher = context.getBean(EventPublisher.class);
        eventPublisher.asyncPublish(binderRefreshBinderEvent);
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
