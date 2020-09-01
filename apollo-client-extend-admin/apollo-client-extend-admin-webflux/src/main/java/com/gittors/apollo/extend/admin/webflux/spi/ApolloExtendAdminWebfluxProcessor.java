package com.gittors.apollo.extend.admin.webflux.spi;

import com.gittors.apollo.extend.common.spi.Ordered;

/**
 * @author zlliu
 * @date 2020/8/30 17:41
 */
public interface ApolloExtendAdminWebfluxProcessor<T> extends Ordered {

    /**
     * Webflux 处理
     * @param request
     * @param objects
     */
    void process(T request, Object... objects);
}
