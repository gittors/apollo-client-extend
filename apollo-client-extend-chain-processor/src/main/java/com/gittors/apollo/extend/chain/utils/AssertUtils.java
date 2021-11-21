package com.gittors.apollo.extend.chain.utils;

import org.apache.commons.lang3.StringUtils;

import java.util.Collection;

/**
 * @author zlliu
 * @date 2020/8/15 11:04
 */
public final class AssertUtils {
    private AssertUtils(){}

    public static void notEmpty(String string, String message) {
        if (StringUtils.isBlank(string)) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void assertNotEmpty(Collection<?> collection, String message) {
        if (collection == null || collection.isEmpty()) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void assertNotBlank(String string, String message) {
        if (StringUtils.isBlank(string)) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void notNull(Object object, String message) {
        if (object == null) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void isTrue(boolean value, String message) {
        if (!value) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void assertState(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }
}
