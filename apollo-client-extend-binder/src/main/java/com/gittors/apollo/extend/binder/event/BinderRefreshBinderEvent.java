package com.gittors.apollo.extend.binder.event;

import com.google.common.collect.Maps;
import lombok.Data;

import java.util.Map;

/**
 * @author zlliu
 * @date 2020/8/20 17:25
 */
@Data
public class BinderRefreshBinderEvent {
    private Map<String, Map<String, String>> data = Maps.newHashMap();

    public BinderRefreshBinderEvent() {
    }

    public BinderRefreshBinderEvent(Map<String, Map<String,String>> data) {
        this.data = data;
    }
}
