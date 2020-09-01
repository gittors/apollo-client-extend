package com.gittors.apollo.extend.support;

import org.apache.commons.collections4.KeyValue;
import org.apache.commons.collections4.keyvalue.AbstractMapEntry;

import java.util.Map;

/**
 * @author zlliu
 * @date 2020/8/22 21:47
 */
public class ApolloExtendStringMapEntry extends AbstractMapEntry<String, String> {
    public ApolloExtendStringMapEntry(String key, String value) {
        super(key, value);
    }

    public ApolloExtendStringMapEntry(KeyValue<? extends String, ? extends String> pair) {
        super(pair.getKey(), pair.getValue());
    }

    public ApolloExtendStringMapEntry(Map.Entry<? extends String, ? extends String> entry) {
        super(entry.getKey(), entry.getValue());
    }
}
