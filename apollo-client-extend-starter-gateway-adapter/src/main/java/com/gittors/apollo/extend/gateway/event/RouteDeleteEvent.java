package com.gittors.apollo.extend.gateway.event;

import lombok.Data;

import java.util.List;

/**
 * @author zlliu
 * @date 2020/7/8 11:29
 */
@Data
public class RouteDeleteEvent<T> {
    private static RouteDeleteEvent INSTANCE = new RouteDeleteEvent();

    /**
     * 数据对象
     */
    private List<T> dataList;

    public static RouteDeleteEvent getInstance() {
        return INSTANCE;
    }
}
