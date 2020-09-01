package com.gittors.apollo.extend.binder.event;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @author zlliu
 * @date 2020/8/20 17:25
 */
@Data
public class BinderRefreshBinderEvent<T> {
    private Map<String, List<T>> data;

    public BinderRefreshBinderEvent() {
    }

    public BinderRefreshBinderEvent(Map<String, List<T>> data) {
        this.data = data;
    }
}
