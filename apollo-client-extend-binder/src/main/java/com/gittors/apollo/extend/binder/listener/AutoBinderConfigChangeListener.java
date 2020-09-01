package com.gittors.apollo.extend.binder.listener;

import com.ctrip.framework.apollo.ConfigChangeListener;
import com.ctrip.framework.apollo.model.ConfigChangeEvent;
import com.gittors.apollo.extend.binder.registry.HolderBeanWrapper;
import com.gittors.apollo.extend.binder.registry.HolderBeanWrapperRegistry;
import com.gittors.apollo.extend.binder.utils.BinderObjectInjector;
import com.gittors.apollo.extend.binder.utils.BinderUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.core.env.Environment;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author zlliu
 * @date 2020/8/10 21:39
 */
public class AutoBinderConfigChangeListener implements ConfigChangeListener {

    private final Environment environment;
    private final BeanFactory beanFactory;
    private final HolderBeanWrapperRegistry holderBeanWrapperRegistry;

    public AutoBinderConfigChangeListener(Environment environment,
                                          BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
        this.environment = environment;
        this.holderBeanWrapperRegistry =
                BinderObjectInjector.getInstance(HolderBeanWrapperRegistry.class);
    }

    @Override
    public void onChange(ConfigChangeEvent changeEvent) {
        Set<String> keys = changeEvent.changedKeys();
        if (CollectionUtils.isEmpty(keys)) {
            return;
        }
        Map<String, Collection<HolderBeanWrapper>> registry =
                holderBeanWrapperRegistry.getRegistry(beanFactory);
        if (registry == null || registry.isEmpty()) {
            return;
        }
        Set<String> keySet = new HashSet<>();
        for (String key : keys) {
            keySet.addAll(
                    registry.keySet()
                            .parallelStream()
                            .filter(bindPrefix -> key.startsWith(bindPrefix))
                            .collect(Collectors.toList())
            );
        }
        for (String binderPrefix : keySet) {
            Collection<HolderBeanWrapper> targetValues = registry.get(binderPrefix);
            for (HolderBeanWrapper holderBeanWrapper : targetValues) {
                BinderUtils.binder(environment, holderBeanWrapper, binderPrefix);
            }
        }
    }

}
