package com.gittors.apollo.extend.common.env;

import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.ConfigChangeListener;
import org.springframework.core.env.EnumerablePropertySource;

import java.util.Set;

/**
 * @author zlliu
 * @date 2022/8/25 11:07
 */
public class SimplePropertySource extends EnumerablePropertySource<Config> {
    private static final String[] EMPTY_ARRAY = new String[0];

    /**
     * 命名空间
     */
    private String namespace;

    public SimplePropertySource(String name, Config source) {
        super(name, source);
    }

    public SimplePropertySource(String name, String namespace, Config source) {
        super(name, source);
        this.namespace = namespace;
    }

    @Override
    public String[] getPropertyNames() {
        Set<String> propertyNames = this.source.getPropertyNames();
        if (propertyNames.isEmpty()) {
            return EMPTY_ARRAY;
        }
        return propertyNames.toArray(new String[propertyNames.size()]);
    }

    @Override
    public Object getProperty(String name) {
        return this.source.getProperty(name, null);
    }

    public void addChangeListener(ConfigChangeListener listener) {
        this.source.addChangeListener(listener);
    }

    public static SimplePropertySource of(String name, SimplePropertySource propertySource) {
        SimplePropertySource simplePropertySource = new SimplePropertySource(name, propertySource.getSource());
        simplePropertySource.setNamespace(propertySource.getNamespace());
        return simplePropertySource;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }
}
