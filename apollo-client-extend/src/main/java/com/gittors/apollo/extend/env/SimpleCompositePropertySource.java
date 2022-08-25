package com.gittors.apollo.extend.env;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.PropertySources;

import java.util.Iterator;

/**
 * @author zlliu
 * @date 2022/8/25 11:07
 */
public class SimpleCompositePropertySource extends CompositePropertySource implements PropertySources {
    /**
     * Create a new {@code CompositePropertySources}.
     *
     * @param name the name of the property source
     */
    public SimpleCompositePropertySource(String name) {
        super(name);
    }

    @Override
    public boolean contains(String name) {
        for (PropertySource<?> propertySource : getPropertySources()) {
            if (propertySource.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public PropertySource<?> get(String name) {
        for (PropertySource<?> propertySource : getPropertySources()) {
            if (propertySource.getName().equals(name)) {
                return propertySource;
            }
        }
        return null;
    }

    @Override
    public Iterator<PropertySource<?>> iterator() {
        return getPropertySources().iterator();
    }

    public boolean remove(String name) {
        if (StringUtils.isBlank(name)) {
            return false;
        }
        for (Iterator<PropertySource<?>> iterator = iterator(); iterator.hasNext();) {
            PropertySource source = iterator.next();
            if (source.getName().equals(name)) {
                iterator.remove();
                return true;
            }
        }
        return false;
    }
}
