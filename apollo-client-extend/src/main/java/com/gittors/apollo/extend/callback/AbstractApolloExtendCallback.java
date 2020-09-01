package com.gittors.apollo.extend.callback;

import java.util.Collections;
import java.util.List;

/**
 * @author zlliu
 * @date 2020/7/8 11:29
 */
public abstract class AbstractApolloExtendCallback implements ApolloExtendCallback {

    @Override
    public List<String> keyList() {
        return Collections.singletonList(getConfigPrefix() + ".*");
    }

    /**
     * 监听的配置前缀
     * @return
     */
    protected abstract String getConfigPrefix();
}
