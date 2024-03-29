package com.gittors.apollo.extend.processor;

import com.ctrip.framework.apollo.spring.config.ConfigPropertySource;
import com.ctrip.framework.apollo.spring.config.ConfigPropertySourceFactory;
import com.ctrip.framework.apollo.spring.property.AutoUpdateConfigChangeListener;
import com.ctrip.framework.apollo.spring.util.SpringInjector;
import com.ctrip.framework.foundation.Foundation;
import com.gittors.apollo.extend.callback.ApolloExtendCallback;
import com.gittors.apollo.extend.common.context.ApolloPropertySourceContext;
import com.gittors.apollo.extend.common.env.SimplePropertySource;
import com.gittors.apollo.extend.context.ApolloExtendContext;
import com.gittors.apollo.extend.utils.ApolloExtendUtils;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author zlliu
 * @date 2022/8/28 17:00
 */
public class AutoUpdateConfigListenerProcessor implements BeanFactoryPostProcessor, EnvironmentAware, PriorityOrdered {
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
        String[] beanNames = beanFactory.getBeanNamesForType(ApolloExtendCallback.class);
        Map<String, ApolloExtendCallback> callbackMap = new HashMap<>(8);
        for (String beanName : beanNames) {
            ApolloExtendCallback callback = beanFactory.getBean(beanName, ApolloExtendCallback.class);
            callbackMap.put(callback.listenKey(), callback);
        }
        //  初始化回调
        ApolloExtendContext.INSTANCE.initCallbackMap(callbackMap);

        AutoUpdateConfigChangeListener updateConfigChangeListener =
                new AutoUpdateConfigChangeListener(environment, beanFactory);
        Collection<SimplePropertySource> simplePropertySources = ApolloPropertySourceContext.INSTANCE.getPropertySources();
        for (SimplePropertySource configPropertySource : simplePropertySources) {
            configPropertySource.addChangeListener(updateConfigChangeListener);
            //  添加自定义监听器
            ApolloExtendUtils.addListener(configPropertySource.getNamespace(), callbackMap);
        }
        List<ConfigPropertySource> configPropertySources = configPropertySourceFactory.getAllConfigPropertySources();
        for (ConfigPropertySource propertySource : configPropertySources) {
            //  添加自定义监听器
            ApolloExtendUtils.addListener(propertySource.getName(), callbackMap);
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
