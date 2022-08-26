package com.gittors.apollo.extend.common.event;

import com.google.common.collect.Maps;
import lombok.Data;

import java.util.Map;

/**
 * @author zlliu
 * @date 2020/8/20 17:25
 */
@Data
public class BinderRefreshBinderEvent {
    private BinderRefreshBinderEvent() {
    }

    /**
     * {Key：命名空间，Value：{配置Key=配置Value}
     */
    private Map<String, Map<String, String>> data = Maps.newHashMap();

    /**
     * 消息来源
     */
    private String source = "UNKNOWN";

    public static BinderRefreshBinderEvent getInstance() {
        return new BinderRefreshBinderEvent();
    }

}
