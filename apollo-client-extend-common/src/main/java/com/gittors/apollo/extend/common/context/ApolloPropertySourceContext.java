package com.gittors.apollo.extend.common.context;

import com.gittors.apollo.extend.common.env.SimplePropertySource;
import org.springframework.core.env.PropertySource;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

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

    public Set<String> getSourceNamespace() {
        return getPropertySources().stream()
                .map(SimplePropertySource::getNamespace).collect(Collectors.toSet());
    }

    public boolean contains(Predicate<PropertySource> predicate) {
        for (PropertySource<?> propertySource : getPropertySources()) {
            if (predicate.test(propertySource)) {
                return true;
            }
        }
        return false;
    }

    public PropertySource<?> get(Predicate<PropertySource> predicate) {
        for (PropertySource<?> propertySource : getPropertySources()) {
            if (predicate.test(propertySource)) {
                return propertySource;
            }
        }
        return null;
    }

}
