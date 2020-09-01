package com.gittors.apollo.extend.event;

/**
 * @author zlliu
 * @date 2020/7/8 11:29
 */
public interface EventPublisher<T> {
    /**
     * 发布异步事件
     *
     * @param object
     */
    void asyncPublish(T object);

    /**
     * 发布同步事件
     * @param object
     */
    void syncPublish(T object);
}
