package com.gittors.apollo.extend.admin.web.spi;

import com.gittors.apollo.extend.common.event.BinderRefreshEvent;
import com.gittors.apollo.extend.event.EventPublisher;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;

import java.util.Map;

/**
 * @author zlliu
 * @date 2020/8/30 17:43
 */
@Slf4j
@Order
public class DefaultApolloExtendAdminProcessor implements ApolloExtendAdminProcessor<ApplicationContext> {
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
     * @param beanFactory
     * @param config
     */
    protected void pushBinder(ApplicationContext beanFactory, Map<String, Map<String, String>> config, String source) {
        BinderRefreshEvent binderRefreshEvent = BinderRefreshEvent.getInstance();
        binderRefreshEvent.setData(config);
        binderRefreshEvent.setSource(source);

        EventPublisher eventPublisher = beanFactory.getBean(EventPublisher.class);
        eventPublisher.asyncPublish(binderRefreshEvent);
    }
}
