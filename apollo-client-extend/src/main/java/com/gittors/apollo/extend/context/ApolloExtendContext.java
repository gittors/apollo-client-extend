package com.gittors.apollo.extend.context;

import com.ctrip.framework.apollo.spring.property.SpringValue;
import com.gittors.apollo.extend.callback.ApolloExtendCallback;
import com.google.common.collect.Maps;

import java.util.Map;

/**
 * @author zlliu
 * @date 2020/7/25 9:02
 */
public enum ApolloExtendContext {
    INSTANCE;

    /**
     * apollo回调缓存
     */
    private Map<String, ApolloExtendCallback> callbackMap;

    private final Object callBackMonitor = new Object();

    /**
     * 缓存：  {@link SpringValue}
     */
    private Map<String, SpringValue> springValueMap = Maps.newLinkedHashMap();

    public void putSpringValueMap(String key, SpringValue value) {
        this.springValueMap.put(key, value);
    }

    public Map<String, SpringValue> getSpringValueMap() {
        return this.springValueMap;
    }

    /**
     * 初始化回调
     * @param callbackMap
     */
    public void initCallbackMap(Map<String, ApolloExtendCallback> callbackMap) {
        synchronized (this.callBackMonitor) {
            this.callbackMap = callbackMap;
        }
    }

    public void putCallBack(String key, ApolloExtendCallback callback) {
        synchronized (this.callBackMonitor) {
            this.callbackMap.put(key, callback);
        }
    }

    public Map<String, ApolloExtendCallback> getCallbackMap() {
        return callbackMap;
    }

}
