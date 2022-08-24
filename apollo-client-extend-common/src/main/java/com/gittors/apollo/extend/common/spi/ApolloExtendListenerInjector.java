package com.gittors.apollo.extend.common.spi;

import java.util.List;

/**
 * @author zlliu
 * @date 2020/9/1 10:57
 */
public interface ApolloExtendListenerInjector<T> {

    /**
     * 注入 监听器
     * @param list
     * @param objects
     */
    void injector(List<T> list, Object... objects);
}
