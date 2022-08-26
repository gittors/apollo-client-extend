package com.gittors.apollo.extend.binder.processor;

import com.ctrip.framework.foundation.Foundation;
import com.gittors.apollo.extend.binder.listener.AutoBinderConfigChangeListener;
import com.gittors.apollo.extend.common.context.ApolloPropertySourceContext;
import com.gittors.apollo.extend.common.env.SimplePropertySource;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;

import java.util.Collection;
import java.util.Set;

/**
 * @author zlliu
 * @date 2020/8/19 21:39
 */
public class BinderPropertySourcesPostProcessor implements BeanFactoryPostProcessor, EnvironmentAware, PriorityOrdered {
    private static final Set<BeanFactory> AUTO_BINDER_INITIALIZED_BEAN_FACTORIES = Sets.newConcurrentHashSet();

    /**
     * 配置属性
     */
    private static final String AUTO_BINDER_CONFIG_KEY = "apollo.autoBinder.injected.enabled";

    private boolean autoBinderInjectedSpringProperties = true;

    private ConfigurableEnvironment environment;

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        initAutoBinderInjectedSpringProperties();
        initializeAutoBinderPropertiesFeature(beanFactory);
    }

    private void initializeAutoBinderPropertiesFeature(ConfigurableListableBeanFactory beanFactory) {
        if (!autoBinderInjectedSpringProperties ||
                !AUTO_BINDER_INITIALIZED_BEAN_FACTORIES.add(beanFactory)) {
            return;
        }

      AutoBinderConfigChangeListener autoBinderConfigChangeListener = new AutoBinderConfigChangeListener(
                environment, beanFactory);

        Collection<SimplePropertySource> configPropertySources = ApolloPropertySourceContext.INSTANCE.getPropertySources();
        for (SimplePropertySource configPropertySource : configPropertySources) {
            configPropertySource.addChangeListener(autoBinderConfigChangeListener);
        }
    }

    private void initAutoBinderInjectedSpringProperties() {
        // 1. Get from System Property
        String enableAutoBinder = System.getProperty(AUTO_BINDER_CONFIG_KEY);
        if (Strings.isNullOrEmpty(enableAutoBinder)) {
            // 2. Get from app.properties
            enableAutoBinder = Foundation.app().getProperty(AUTO_BINDER_CONFIG_KEY, null);
        }
        if (!Strings.isNullOrEmpty(enableAutoBinder)) {
            autoBinderInjectedSpringProperties = Boolean.parseBoolean(enableAutoBinder.trim());
        }
    }

    @Override
    public void setEnvironment(Environment environment) {
        //it is safe enough to cast as all known environment is derived from ConfigurableEnvironment
        this.environment = (ConfigurableEnvironment) environment;
    }

    @Override
    public int getOrder() {
        //make it as early as possible
        return Ordered.HIGHEST_PRECEDENCE + 10;
    }

}
