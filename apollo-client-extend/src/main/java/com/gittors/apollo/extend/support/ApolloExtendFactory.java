package com.gittors.apollo.extend.support;

import java.util.Map;
import java.util.Set;

/**
 * @author zlliu
 * @date 2022/8/24 18:05
 */
public interface ApolloExtendFactory {

    @FunctionalInterface
    interface FilterPredicate {
        /**
         * 断言
         * @param key
         * @param configEntry
         * @return
         */
        boolean match(String key, Map.Entry<Boolean, Set<String>> configEntry);
    }

}
