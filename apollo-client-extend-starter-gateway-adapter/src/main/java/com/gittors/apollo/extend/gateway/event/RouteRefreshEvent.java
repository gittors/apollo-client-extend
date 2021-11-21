package com.gittors.apollo.extend.gateway.event;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * @author zlliu
 * @date 2020/7/8 11:29
 */
@Data
public class RouteRefreshEvent {
    public RouteRefreshEvent() {
    }

    /**
     * {Key：配置Key，Value：配置Value}
     */
    private Map<String, String> data = new HashMap<>();

    private static RouteRefreshEvent INSTANCE = new RouteRefreshEvent();

    public static RouteRefreshEvent getInstance() {
        return INSTANCE;
    }
}
