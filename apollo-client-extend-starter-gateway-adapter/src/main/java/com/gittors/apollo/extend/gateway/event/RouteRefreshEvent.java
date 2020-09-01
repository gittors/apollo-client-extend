package com.gittors.apollo.extend.gateway.event;

/**
 * @author zlliu
 * @date 2020/7/8 11:29
 */
public class RouteRefreshEvent {
    private static RouteRefreshEvent INSTANCE = new RouteRefreshEvent();

    public static RouteRefreshEvent getInstance() {
        return INSTANCE;
    }
}
