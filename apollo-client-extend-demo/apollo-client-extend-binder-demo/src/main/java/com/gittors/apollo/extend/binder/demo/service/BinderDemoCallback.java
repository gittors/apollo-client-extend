package com.gittors.apollo.extend.binder.demo.service;

import com.gittors.apollo.extend.binder.demo.event.EventPublishDelegate;
import com.gittors.apollo.extend.service.ApolloExtendCallbackAdapter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Map;

/**
 * apollo.extend.namespace 配置更新回调
 *
 * @author zlliu
 * @date 2020/8/19 11:18
 */
@Slf4j
public class BinderDemoCallback extends ApolloExtendCallbackAdapter {
    private ConfigurableListableBeanFactory beanFactory;

    public BinderDemoCallback(ConfigurableApplicationContext context) {
        this.beanFactory = context.getBeanFactory();
    }

    @Override
    protected void changeProcess(Map<String, Map<String, String>> data) {
        if (MapUtils.isEmpty(data)) {
            log.warn("#changeProcess configMap is empty!");
            return;
        }
        EventPublishDelegate.publish(beanFactory, data, "BinderDemoCallback#changeProcess");
    }

}
