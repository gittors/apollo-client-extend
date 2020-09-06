package com.gittors.apollo.extend.binder.event;

import com.gittors.apollo.extend.binder.registry.HolderBeanWrapper;
import com.gittors.apollo.extend.binder.registry.HolderBeanWrapperRegistry;
import com.gittors.apollo.extend.binder.utils.BinderObjectInjector;
import com.gittors.apollo.extend.binder.utils.BinderUtils;
import com.google.common.collect.Maps;
import com.google.common.eventbus.Subscribe;
import com.nepxion.eventbus.annotation.EventBus;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.core.env.Environment;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author zlliu
 * @date 2020/7/8 11:29
 */
@EventBus
public class BinderEventSubscriber {

    public static final String BEAN_NAME = "binderEventSubscriber";

    private final Environment environment;

    private final HolderBeanWrapperRegistry holderBeanWrapperRegistry;
    private final BeanFactory beanFactory;

    public BinderEventSubscriber(BeanFactory beanFactory, Environment environment) {
        this.holderBeanWrapperRegistry =
                BinderObjectInjector.getInstance(HolderBeanWrapperRegistry.class);
        this.beanFactory = beanFactory;
        this.environment = environment;
    }

    @Subscribe
    public void refreshBinder(BinderRefreshBinderEvent event) {
        Map<String, Map<String, String>> data = event.getData();
        Map<String, String> allMap = Maps.newHashMap();
        data.values().forEach(map -> allMap.putAll(map));

        Map<String, Collection<HolderBeanWrapper>> registry = holderBeanWrapperRegistry.getRegistry(beanFactory);
        if (MapUtils.isEmpty(registry)) {
            return;
        }

        Set<String> keySet = new HashSet<>();
        for (String key : allMap.keySet()) {
            keySet.addAll(
                    registry.keySet()
                            .parallelStream()
                            .filter(bindPrefix -> key.startsWith(bindPrefix))
                            .collect(Collectors.toList())
            );
        }
        for (String binderPrefix : keySet) {
            Collection<HolderBeanWrapper> targetValues = registry.get(binderPrefix);
            for (HolderBeanWrapper propertiesWrapper : targetValues) {
                BinderUtils.binder(environment, propertiesWrapper, binderPrefix);
            }
        }
    }
}
