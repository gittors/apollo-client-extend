package com.gittors.apollo.extend.chain.chain;

import com.gittors.apollo.extend.chain.context.Context;

import java.util.Map;

/**
 * @author zlliu
 * @date 2020/8/14 22:27
 */
public interface Processor<T> {
    /**
     * Entrance of this node.
     *
     * @param context         current {@link Context}
     * @param param           generics parameter
     * @param args            parameters of the original call
     * @throws Throwable
     */
    void entry(Context context, T param, Map<String, Object> args) throws Throwable;

    /**
     * Means finish of {@link #entry(Context, Object, Map<String, Object>)}.
     *
     * @param context         current {@link Context}
     * @param obj             relevant object
     * @param args            parameters of the original call
     * @throws Throwable
     */
    void fireEntry(Context context, Object obj, Map<String, Object> args) throws Throwable;

}
