package com.gittors.apollo.extend.context;

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
    private Map<String, ApolloExtendCallback> callbackMap = Maps.newHashMap();

    private final Object callBackMonitor = new Object();

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
