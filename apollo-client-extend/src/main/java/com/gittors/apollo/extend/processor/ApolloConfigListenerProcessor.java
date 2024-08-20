package com.gittors.apollo.extend.processor;

import com.ctrip.framework.apollo.spring.config.ConfigPropertySource;
import com.ctrip.framework.apollo.spring.config.ConfigPropertySourceFactory;
import com.ctrip.framework.apollo.spring.property.AutoUpdateConfigChangeListener;
import com.ctrip.framework.apollo.spring.util.SpringInjector;
import com.ctrip.framework.foundation.Foundation;
import com.gittors.apollo.extend.common.context.ApolloPropertySourceContext;
import com.gittors.apollo.extend.common.env.SimplePropertySource;
import com.gittors.apollo.extend.service.CustomiseConfigChangeListener;
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
import java.util.List;
import java.util.Set;

/**
 * 给命名空间注入监听器
 *
 * @author zlliu
 * @date 2022/8/28 17:00
 */
public class ApolloConfigListenerProcessor implements BeanFactoryPostProcessor, EnvironmentAware, PriorityOrdered {
    private static final Set<BeanFactory> AUTO_BINDER_INITIALIZED_BEAN_FACTORIES = Sets.newConcurrentHashSet();

    private static final String AUTO_UPDATE_CONFIG_KEY = "apollo.autoUpdate.injected.enabled";

    private final ConfigPropertySourceFactory configPropertySourceFactory =
            SpringInjector.getInstance(ConfigPropertySourceFactory.class);

    private boolean autoUpdateConfigListenerProcessor = true;

    private ConfigurableEnvironment environment;

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        initSpringProperties();
        initializeAutoUpdatePropertiesFeature(beanFactory);
    }

    private void initializeAutoUpdatePropertiesFeature(ConfigurableListableBeanFactory beanFactory) {
        if (!autoUpdateConfigListenerProcessor ||
                !AUTO_BINDER_INITIALIZED_BEAN_FACTORIES.add(beanFactory)) {
            return;
        }
        //  step 1: application 命名空间添加自定义监听器(由于application命名空间由Apollo添加了自动更新监听器，所以不需要重复添加)
        List<ConfigPropertySource> configPropertySources = configPropertySourceFactory.getAllConfigPropertySources();
        CustomiseConfigChangeListener customiseConfigChangeListener = beanFactory.getBean(CustomiseConfigChangeListener.class);
        for (ConfigPropertySource propertySource : configPropertySources) {
            //  添加自定义监听器
            propertySource.addChangeListener(customiseConfigChangeListener);
        }

        //  step 2: 其他命名空间添加自定义监听器
        AutoUpdateConfigChangeListener updateConfigChangeListener = new AutoUpdateConfigChangeListener(environment, beanFactory);
        Collection<SimplePropertySource> simplePropertySources = ApolloPropertySourceContext.INSTANCE.getPropertySources();
        for (SimplePropertySource configPropertySource : simplePropertySources) {
            //  添加Apollo自动更新监听器
            configPropertySource.addChangeListener(updateConfigChangeListener);
            //  添加自定义监听器
            configPropertySource.addChangeListener(customiseConfigChangeListener);
        }
    }

    private void initSpringProperties() {
        // 1. Get from System Property
        String enableAutoUpdate = System.getProperty(AUTO_UPDATE_CONFIG_KEY);
        if (Strings.isNullOrEmpty(enableAutoUpdate)) {
            // 2. Get from app.properties
            enableAutoUpdate = Foundation.app().getProperty(AUTO_UPDATE_CONFIG_KEY, null);
        }
        if (!Strings.isNullOrEmpty(enableAutoUpdate)) {
            autoUpdateConfigListenerProcessor = Boolean.parseBoolean(enableAutoUpdate.trim());
        }
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = (ConfigurableEnvironment) environment;
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 100;
    }

}
