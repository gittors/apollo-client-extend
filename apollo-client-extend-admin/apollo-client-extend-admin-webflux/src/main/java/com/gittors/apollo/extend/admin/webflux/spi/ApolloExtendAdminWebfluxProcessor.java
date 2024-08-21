package com.gittors.apollo.extend.admin.webflux.spi;

/**
 * @author zlliu
 * @date 2020/8/30 17:41
 */
public interface ApolloExtendAdminWebfluxProcessor<T> {

    /**
     * Webflux 处理
     * @param request
     * @param objects
     */
    void process(T request, Object... objects);
}
