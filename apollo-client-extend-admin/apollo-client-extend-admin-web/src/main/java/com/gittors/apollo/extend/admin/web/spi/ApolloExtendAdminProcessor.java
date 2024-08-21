package com.gittors.apollo.extend.admin.web.spi;

/**
 * @author zlliu
 * @date 2020/8/30 17:41
 */
public interface ApolloExtendAdminProcessor<T> {

    /**
     * Web 处理
     * @param request
     * @param objects
     */
    void process(T request, Object... objects);
}
