package com.gittors.apollo.extend.service;

import com.ctrip.framework.apollo.model.ConfigChangeEvent;

/**
 * @author zlliu
 * @date 2024/8/20 22:24
 */
public interface ApolloConfigListenerHandler {
    /**
     * 监听处理
     */
    void handle(ConfigChangeEvent changeEvent);
}
