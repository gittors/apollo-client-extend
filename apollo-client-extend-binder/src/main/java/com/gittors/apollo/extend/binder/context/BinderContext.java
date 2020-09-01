package com.gittors.apollo.extend.binder.context;

import com.google.common.collect.Maps;

import java.util.Map;

/**
 * @author zlliu
 * @date 2020/8/10 21:39
 */
public enum BinderContext {
    INSTANCE;

    /**
     * 以 @ConfigurationProperties 为例：
     * {Key: @ConfigurationProperties 注解的 prefix值，Value：@ConfigurationProperties 注解标注对象的Class}
     */
    private Map<String, Class<?>> binderMap = Maps.newLinkedHashMap();

    public void initBinderMap(Map<String, Class<?>> propertiesMap) {
        this.binderMap = propertiesMap;
    }

    public Map<String, Class<?>> getBinderMap() {
        return binderMap;
    }
}
