package com.gittors.apollo.extend.service;

import com.ctrip.framework.apollo.model.ConfigChangeEvent;

/**
 * @author zlliu
 * @date 2024/8/19 0019 22:04
 */
public interface SimpleConfigListenerHandler {
    /**
     * KEY匹配
     * @param key
     * @return
     */
    boolean match(String key);

    /**
     * 监听处理
     */
    void handle(ConfigChangeEvent changeEvent);
}
