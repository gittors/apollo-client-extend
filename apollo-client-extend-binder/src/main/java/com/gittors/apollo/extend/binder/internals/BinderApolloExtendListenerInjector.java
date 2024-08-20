package com.gittors.apollo.extend.binder.internals;

import com.ctrip.framework.apollo.ConfigChangeListener;
import com.gittors.apollo.extend.binder.listener.AutoBinderConfigChangeListener;
import com.gittors.apollo.extend.common.spi.ApolloExtendListenerInjector;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.List;

/**
 * @author zlliu
 * @date 2020/9/1 11:02
 */
public class BinderApolloExtendListenerInjector implements ApolloExtendListenerInjector<ConfigChangeListener> {

    @Override
    public void injector(List<ConfigChangeListener> list, Object... objects) {
        ConfigurableApplicationContext context = (ConfigurableApplicationContext) objects[0];

        list.add(new AutoBinderConfigChangeListener(context));
    }
}
