package com.gittors.apollo.extend.binder.internals;

import com.ctrip.framework.apollo.ConfigChangeListener;
import com.gittors.apollo.extend.binder.listener.AutoBinderConfigChangeListener;
import com.gittors.apollo.extend.common.spi.ApolloExtendListenerInjector;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.core.env.Environment;

import java.util.List;

/**
 * @author zlliu
 * @date 2020/9/1 11:02
 */
public class BinderApolloExtendListenerInjector implements ApolloExtendListenerInjector<ConfigChangeListener> {

    @Override
    public void injector(List<ConfigChangeListener> list, Object... objects) {
        Environment environment = (Environment) objects[0];
        BeanFactory beanFactory = (BeanFactory) objects[1];

        AutoBinderConfigChangeListener autoBinderConfigChangeListener =
                new AutoBinderConfigChangeListener(environment, beanFactory);
        list.add(autoBinderConfigChangeListener);
    }
}
