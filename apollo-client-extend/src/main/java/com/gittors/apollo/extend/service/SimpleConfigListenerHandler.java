package com.gittors.apollo.extend.service;

/**
 * @author zlliu
 * @date 2024/8/19 0019 22:04
 */
public interface SimpleConfigListenerHandler extends ApolloConfigListenerHandler {
    /**
     * KEY匹配
     * @param key
     * @return
     */
    boolean match(String key);
}
