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
    /**
     * {Key：命名空间，Value：{配置Key=配置Value}}
     */
    private Map<String, String> data = Maps.newHashMap();

    public BinderRefreshBinderEvent() {
    }

    public BinderRefreshBinderEvent(Map<String,String> data) {
        this.data = data;
    }
}
