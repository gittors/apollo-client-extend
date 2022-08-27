package com.gittors.apollo.extend.binder.internals;

import com.ctrip.framework.apollo.ConfigChangeListener;
import com.gittors.apollo.extend.binder.listener.AutoBinderConfigChangeListener;
import com.gittors.apollo.extend.common.spi.ApolloExtendListenerInjector;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.env.ConfigurableEnvironment;

import java.util.List;

/**
 * @author zlliu
 * @date 2020/9/1 11:02
 */
public class BinderApolloExtendListenerInjector implements ApolloExtendListenerInjector<ConfigChangeListener> {

    @Override
    public void injector(List<ConfigChangeListener> list, Object... objects) {
        ConfigurableEnvironment environment = (ConfigurableEnvironment) objects[0];
        ConfigurableListableBeanFactory beanFactory = (ConfigurableListableBeanFactory) objects[1];

        list.add(new AutoBinderConfigChangeListener(environment, beanFactory));
    }
}
