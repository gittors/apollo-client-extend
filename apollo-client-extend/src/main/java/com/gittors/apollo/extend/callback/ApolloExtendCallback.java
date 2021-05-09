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
     * @param oldValue  配置key旧值
     * @param newValue  配置key新值
     * @param objects
     */
    void callback(String oldValue, String newValue, Object... objects);

    /**
     * 监听key集合
     * @return
     */
    List<String> keyList();
}
