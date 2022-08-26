package com.gittors.apollo.extend.common.context;

import com.gittors.apollo.extend.common.env.SimplePropertySource;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author zlliu
 * @date 2022/8/25 19:02
 */
public enum ApolloPropertySourceContext {
    INSTANCE;

    /**
     * PropertySource 缓存
     */
    private final Set<SimplePropertySource> propertySources = new LinkedHashSet<>();

    public Collection<SimplePropertySource> getPropertySources() {
        return this.propertySources;
    }

}
