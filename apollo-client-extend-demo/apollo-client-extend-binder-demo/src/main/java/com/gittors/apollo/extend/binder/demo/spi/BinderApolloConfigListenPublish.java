package com.gittors.apollo.extend.binder.demo.spi;

import com.gittors.apollo.extend.binder.demo.event.EventPublishDelegate;
import com.gittors.apollo.extend.spi.ApolloExtendListenPublish;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.annotation.Order;

import java.util.Map;

/**
 * listen.key 配置更新回调
 *
 * @author zlliu
 * @date 2022/8/23 11:18
 */
@Order(0)
public class BinderApolloConfigListenPublish implements ApolloExtendListenPublish<Map<String, Map<String, String>>> {

    @Override
    public void doPublish(ConfigurableApplicationContext context, Map<String, Map<String, String>> data) {
        EventPublishDelegate.publish(context, data, "BinderApolloExtendListenPublish#doPublish");
    }
}
