package com.gittors.apollo.extend.test;

import com.gittors.apollo.extend.utils.ApolloExtendUtils;
import com.google.common.collect.Sets;

import java.util.AbstractMap;
import java.util.Map;
import java.util.Set;

/**
 * @author zlliu
 * @date 2020/12/13 17:53
 */
public class ApolloExtendUtilsTest {
    public static void main(String[] args) {
        String key = "key";
        Map.Entry<Boolean, Set<String>> configEntry =
                new AbstractMap.SimpleEntry<>(Boolean.TRUE, Sets.newHashSet("key", "key2"));
        boolean result = ApolloExtendUtils.predicateMatch(key, configEntry);
        System.out.println(result);
    }
}
