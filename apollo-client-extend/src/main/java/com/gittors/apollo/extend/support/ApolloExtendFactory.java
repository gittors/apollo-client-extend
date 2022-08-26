package com.gittors.apollo.extend.support;

import com.gittors.apollo.extend.common.env.SimplePropertySource;

import java.util.Map;
import java.util.Set;

/**
 * @author zlliu
 * @date 2022/8/24 18:05
 */
public interface ApolloExtendFactory {

    @FunctionalInterface
    interface PropertyFilterPredicate {
        /**
         * 断言
         * @param key
         * @param configEntry
         * @return
         */
        boolean match(String key, Map.Entry<Boolean, Set<String>> configEntry);
    }

    @FunctionalInterface
    interface DataFilter {
        /**
         * 数据过滤
         * @param propertySource
         * @param configEntry
         * @return
         */
        Map<String, String> filter(SimplePropertySource propertySource, Map.Entry<Boolean, Set<String>> configEntry);
    }

    @FunctionalInterface
    interface PropertySourceFilterPredicate {
        /**
         * propertySource 断言
         * @param propertySource
         * @return
         */
        boolean match(SimplePropertySource propertySource);
    }

    @FunctionalInterface
    interface PropertySourceFactory {
        /**
         * 创建 propertySource
         * @param created   是否创建
         * @param cached    是否缓存
         * @param propertySource    待缓存的对象
         * @return
         */
        SimplePropertySource createPropertySource(boolean created, boolean cached, SimplePropertySource propertySource);
    }

}
