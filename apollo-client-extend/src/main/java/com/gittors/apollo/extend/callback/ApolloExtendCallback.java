package com.gittors.apollo.extend.callback;

import java.util.List;

/**
 *
 * @author zlliu
 * @date 2020/7/8 17:23
 */
public interface ApolloExtendCallback {
    /**
     * Apollo配置更新回调
     * @param namespace
     * @param oldValue
     * @param newValue
     * @param timestamp
     */
    void callback(String namespace, String oldValue, String newValue, long timestamp);

    /**
     * 监听key集合
     * @return
     */
    List<String> keyList();
}
