package com.gittors.apollo.extend.binder.listener;

import com.ctrip.framework.apollo.ConfigChangeListener;
import com.ctrip.framework.apollo.model.ConfigChangeEvent;
import com.gittors.apollo.extend.binder.registry.HolderBeanWrapper;
import com.gittors.apollo.extend.binder.registry.HolderBeanWrapperRegistry;
import com.gittors.apollo.extend.binder.utils.BinderObjectInjector;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.ConfigurableApplicationContext;
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
@Slf4j
public class AutoBinderConfigChangeListener implements ConfigChangeListener {

    private final ConfigurableApplicationContext context;
    private final HolderBeanWrapperRegistry beanWrapperRegistry;

    public AutoBinderConfigChangeListener(ConfigurableApplicationContext context) {
        this.context = context;
        this.beanWrapperRegistry = BinderObjectInjector.getInstance(HolderBeanWrapperRegistry.class);
    }

    @Override
    public void onChange(ConfigChangeEvent changeEvent) {
        Set<String> keys = changeEvent.changedKeys();
        if (CollectionUtils.isEmpty(keys)) {
            return;
        }
        Map<String, Collection<HolderBeanWrapper>> registry =
                beanWrapperRegistry.getRegistry(context.getBeanFactory());
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
        Binder binder = Binder.get(context.getEnvironment());
        for (String binderPrefix : keySet) {
            Collection<HolderBeanWrapper> targetValues = registry.get(binderPrefix);
            for (HolderBeanWrapper holderBeanWrapper : targetValues) {
                Object value = binder.bind(binderPrefix, Bindable.of(holderBeanWrapper.getField().getType()))
                        .orElse(null);
                if (value != null) {
                    try {
                        holderBeanWrapper.update(value);
                    } catch (Throwable ex) {
                        log.warn("#refreshBinder binder failed: ", ex);
                    }
                }
            }
        }
    }

}
