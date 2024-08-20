package com.gittors.apollo.extend.binder.demo.service;

import com.gittors.apollo.extend.binder.demo.event.EventPublishDelegate;
import com.gittors.apollo.extend.spi.ApolloExtendConfigPostProcessor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Map;

/**
 * apollo.extend.namespace 配置更新回调
 *
 * @author zlliu
 * @date 2020/8/19 11:18
 */
@Slf4j
public class BinderConfigPostProcessor implements ApolloExtendConfigPostProcessor<Map<String, Map<String, String>>> {

    @Override
    public void postProcess(ConfigurableApplicationContext applicationContext, Map<String, Map<String, String>> data) {
        if (MapUtils.isEmpty(data)) {
            log.warn("#changeProcess configMap is empty!");
            return;
        }
        EventPublishDelegate.publish(applicationContext, data, "BinderDemoCallback#changeProcess");
    }

    @Override
    public int getOrder() {
        return 0;
    }

}
