package com.gittors.apollo.extend.gateway.demo.event;

import com.gittors.apollo.extend.gateway.event.RouteRefreshEvent;
import com.google.common.eventbus.Subscribe;
import com.nepxion.eventbus.annotation.EventBus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author zlliu
 * @date 2020/7/8 11:29
 */
@Slf4j
@Component
@EventBus
public class GatewayEventSubscriber {

    @Subscribe
    public void refreshRoute(RouteRefreshEvent event) {
        //  1.如果路由配置有更新: GatewayExtendCallback 会发布路由更新事件，此处监听即可

        //  2.然后自定义路由更新逻辑: event.getData()是更新的配置项
        //  do something...
    }
}
