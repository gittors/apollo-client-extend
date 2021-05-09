package com.gittors.apollo.extend.common.utils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * @author zlliu
 * @date 2020/8/20 21:50
 */
public final class ListUtils {
    private ListUtils() {
    }

    /**
     * 集合去重：
     *  第一个能放进去，第二个放不进去
     * @param keyExtractor
     * @param <T>
     * @return
     */
    public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Map<Object, Boolean> seen = new ConcurrentHashMap(8);
        return (t) -> {
            return seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
        };
    }

}
