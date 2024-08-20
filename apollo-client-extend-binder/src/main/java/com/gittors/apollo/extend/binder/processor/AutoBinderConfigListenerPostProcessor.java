package com.gittors.apollo.extend.binder.processor;

import com.ctrip.framework.apollo.spring.config.ConfigPropertySource;
import com.ctrip.framework.apollo.spring.config.ConfigPropertySourceFactory;
import com.ctrip.framework.apollo.spring.util.SpringInjector;
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
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * @author zlliu
 * @date 2020/8/19 21:39
 */
public class AutoBinderConfigListenerPostProcessor implements BeanFactoryPostProcessor, ApplicationContextAware, PriorityOrdered {
    private static final Set<BeanFactory> AUTO_BINDER_INITIALIZED_BEAN_FACTORIES = Sets.newConcurrentHashSet();

    private final ConfigPropertySourceFactory configPropertySourceFactory =
            SpringInjector.getInstance(ConfigPropertySourceFactory.class);

    private static final String AUTO_BINDER_CONFIG_KEY = "apollo.extend.auto.binder.enabled";

    private boolean autoBinderInjectedSpringProperties = true;

    private ConfigurableApplicationContext context;

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
        AutoBinderConfigChangeListener binderConfigChangeListener = new AutoBinderConfigChangeListener(context);
        Collection<SimplePropertySource> configPropertySources = ApolloPropertySourceContext.INSTANCE.getPropertySources();
        for (SimplePropertySource configPropertySource : configPropertySources) {
            //  其他命名空间添加自动绑定监听器
            configPropertySource.addChangeListener(binderConfigChangeListener);
        }
        List<ConfigPropertySource> propertySources = configPropertySourceFactory.getAllConfigPropertySources();
        for (ConfigPropertySource propertySource : propertySources) {
            //  application 命名空间添加自动绑定监听器
            propertySource.addChangeListener(binderConfigChangeListener);
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
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = (ConfigurableApplicationContext) applicationContext;
    }

    @Override
    public int getOrder() {
        //make it as early as possible
        return Ordered.HIGHEST_PRECEDENCE + 10;
    }

}
