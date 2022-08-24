package com.gittors.apollo.extend.spi;

import com.ctrip.framework.apollo.model.ConfigChange;
import com.ctrip.framework.apollo.model.ConfigChangeEvent;
import com.gittors.apollo.extend.callback.ApolloExtendCallback;
import com.gittors.apollo.extend.common.service.Ordered;
import com.google.common.collect.Maps;
import org.apache.commons.collections4.MapUtils;

import java.util.Map;
import java.util.Set;

/**
 * @author zlliu
 * @date 2022/08/21 13:14
 */
public class DefaultApolloConfigChangeCallBack implements ApolloConfigChangeCallBack {

    @Override
    public void callBack(Map<String, ApolloExtendCallback> callbackMap, ConfigChangeEvent changeEvent) {
        Map<String, ApolloExtendCallback> map = recordCallback(callbackMap, changeEvent.changedKeys());
        if (MapUtils.isEmpty(map)) {
            return;
        }
        for (Map.Entry<String, ApolloExtendCallback> callbackEntry : map.entrySet()) {
            ApolloExtendCallback callback = callbackEntry.getValue();
            //  分两种情况：
            //  1.如果是一个模糊的监听KEY,如：gittors.gateway.dynamic.*, 这种配置不关心每个key的修改值，即oldValue 和 newValue可以传空值
            //  2.如果是一个确定的监听KEY,如：gittors.gateway.dynamic, 则可以通过 ChangeEvent 拿到oldValue 和 newValue值
            ConfigChange configChange = changeEvent.getChange(callbackEntry.getKey());
            //  能拿到 configChange, 说明是确定的key
            if (configChange != null) {
                callback.callback(configChange.getOldValue(), configChange.getNewValue(), callbackEntry.getKey(), changeEvent);
            } else {
                callback.callback(null, null, changeEvent);
            }
        }
    }

    /**
     * 根据监听的KEY和Apollo 的变更的KEY，找到对应的回调
     * @param callbackMap   配置的监听KEY的回调集合
     * @param changedKeys   Apollo 变更的KEY集合
     * @return
     */
    protected Map<String, ApolloExtendCallback> recordCallback(Map<String, ApolloExtendCallback> callbackMap, Set<String> changedKeys) {
        Map<String, ApolloExtendCallback> map = Maps.newHashMap();
        for (Map.Entry<String, ApolloExtendCallback> entry : callbackMap.entrySet()) {
            for (String key : changedKeys) {
                // 精确匹配和模糊匹配
                if (key.equals(entry.getKey()) ||
                        (entry.getKey().indexOf("*") > 0 && key.indexOf(entry.getKey().replace("*","")) >= 0)) {
                    map.put(entry.getKey(), entry.getValue());
                }
            }
        }
        return map;
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
