package com.gittors.apollo.extend.spi;

import com.ctrip.framework.apollo.model.ConfigChangeEvent;
import com.gittors.apollo.extend.callback.ApolloExtendCallback;
import com.gittors.apollo.extend.common.service.Ordered;

import java.util.Map;

/**
 * @author zlliu
 * @date 2022/08/21 13:14
 */
public interface ApolloConfigChangeCallBack extends Ordered {

    /**
     * 根据监听key
     * @param callbackMap
     * @return
     */
    void callBack(Map<String, ApolloExtendCallback> callbackMap, ConfigChangeEvent changeEvent);
}
