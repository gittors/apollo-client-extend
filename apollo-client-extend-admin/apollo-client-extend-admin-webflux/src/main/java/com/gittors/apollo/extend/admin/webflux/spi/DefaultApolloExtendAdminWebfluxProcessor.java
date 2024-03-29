package com.gittors.apollo.extend.admin.webflux.spi;

import com.ctrip.framework.apollo.core.spi.Ordered;
import com.gittors.apollo.extend.common.event.BinderRefreshBinderEvent;
import com.gittors.apollo.extend.event.EventPublisher;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.springframework.context.ApplicationContext;

import java.util.Map;

/**
 * @author zlliu
 * @date 2020/8/30 17:43
 */
@Slf4j
public class DefaultApolloExtendAdminWebfluxProcessor implements ApolloExtendAdminWebfluxProcessor<ApplicationContext> {
    @Override
    public void process(ApplicationContext request, Object... objects) {
        Map<String, Map<String, String>> config = (Map<String, Map<String, String>>) objects[0];
        if (MapUtils.isEmpty(config)) {
            log.warn("#process config is empty!");
        }
        pushBinder(request, config, (String) objects[1]);
    }

    /**
     * 推送绑定事件
     * @param context
     * @param config
     */
    protected void pushBinder(ApplicationContext context, Map<String, Map<String, String>> config, String source) {
        BinderRefreshBinderEvent binderRefreshBinderEvent = BinderRefreshBinderEvent.getInstance();
        binderRefreshBinderEvent.setData(config);
        binderRefreshBinderEvent.setSource(source);
        EventPublisher eventPublisher = context.getBean(EventPublisher.class);
        eventPublisher.asyncPublish(binderRefreshBinderEvent);
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
