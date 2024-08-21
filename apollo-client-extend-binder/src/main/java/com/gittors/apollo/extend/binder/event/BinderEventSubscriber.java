package com.gittors.apollo.extend.binder.event;

import com.gittors.apollo.extend.binder.registry.HolderBeanWrapper;
import com.gittors.apollo.extend.binder.registry.HolderBeanWrapperRegistry;
import com.gittors.apollo.extend.binder.utils.BinderObjectInjector;
import com.gittors.apollo.extend.common.event.BinderRefreshEvent;
import com.google.common.eventbus.Subscribe;
import com.nepxion.eventbus.annotation.EventBus;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author zlliu
 * @date 2020/7/8 11:29
 */
@Slf4j
@EventBus
public class BinderEventSubscriber {

    private final HolderBeanWrapperRegistry holderBeanWrapperRegistry;
    private final ConfigurableApplicationContext context;

    public BinderEventSubscriber(ConfigurableApplicationContext context) {
        this.holderBeanWrapperRegistry = BinderObjectInjector.getInstance(HolderBeanWrapperRegistry.class);
        this.context = context;
    }

    /**
     * 监听绑定事件，并刷新对象
     * @param event 事件对象
     */
    @Subscribe
    public void refreshBinder(BinderRefreshEvent event) {
        //  所有待刷新的配置：{key:配置key,value:配置value}
        Map<String, Map<String, String>> dataMap = event.getData();

        //  根据bean工厂获得注册工厂
        Map<String, Collection<HolderBeanWrapper>> registry = holderBeanWrapperRegistry.getRegistry(context.getBeanFactory());
        if (MapUtils.isEmpty(registry) || MapUtils.isEmpty(dataMap)) {
            log.warn("#refreshBinder skip refreshBinder,source: [{}] registry OR dataMap is empty!", event.getSource());
            return;
        }
        Set<String> keyPrefixSet = new HashSet<>();
        for (Map.Entry<String, Map<String, String>> dataEntry : dataMap.entrySet()) {
            Set<String> prefixSet = new HashSet<>();
            //  循环待刷新的key,获得待刷新的key前缀配置
            for (String key : dataEntry.getValue().keySet()) {
                prefixSet.addAll(registry.keySet().parallelStream()
                        .filter(bindPrefix -> key.startsWith(bindPrefix) || bindPrefix.startsWith(key))
                        .collect(Collectors.toList())
                );
            }
            log.info("#refreshBinder namespace:[{}], listener key set: {}", dataEntry.getKey(), prefixSet);
            keyPrefixSet.addAll(prefixSet);
        }
        if (CollectionUtils.isEmpty(keyPrefixSet)) {
            log.warn("#refreshBinder skip refreshBinder,source: [{}] keyPrefixSet is empty!", event.getSource());
            return;
        }
        Binder binder = Binder.get(context.getEnvironment());
        for (String binderPrefix : keyPrefixSet) {
            //  根据配置key前缀，获得bean绑定对象wrapper
            Collection<HolderBeanWrapper> targetValues = registry.get(binderPrefix);
            for (HolderBeanWrapper propertiesWrapper : targetValues) {
                Object value = binder.bind(binderPrefix, Bindable.of(propertiesWrapper.getField().getType()))
                        .orElse(null);
                if (value != null) {
                    try {
                        propertiesWrapper.update(value);
                    } catch (Throwable ex) {
                        log.error("#refreshBinder binder failed, source: [{}] : ", event.getSource(), ex);
                    }
                }
            }
        }
    }
}
