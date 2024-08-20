package com.gittors.apollo.extend.service;

import java.util.Set;

/**
 * 多对一的监听回调，比如服务网关配置，多个KEY更新后只需要回调一次
 *
 * @author zlliu
 * @date 2024/8/19 22:04
 */
public interface CompositeConfigListenerHandler extends ApolloConfigListenerHandler {
    /**
     * KEY匹配
     * @param keys
     * @return
     */
    boolean match(Set<String> keys);
}
