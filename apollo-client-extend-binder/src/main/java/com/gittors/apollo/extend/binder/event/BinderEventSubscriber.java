package com.gittors.apollo.extend.binder.event;

import com.gittors.apollo.extend.binder.registry.HolderBeanWrapper;
import com.gittors.apollo.extend.binder.registry.HolderBeanWrapperRegistry;
import com.gittors.apollo.extend.binder.utils.BinderObjectInjector;
import com.gittors.apollo.extend.common.event.BinderRefreshBinderEvent;
import com.google.common.eventbus.Subscribe;
import com.nepxion.eventbus.annotation.EventBus;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
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
@Slf4j
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

    /**
     * 监听绑定事件，并刷新对象
     * @param event 事件对象
     */
    @Subscribe
    public void refreshBinder(BinderRefreshBinderEvent event) {
        //  所有待刷新的配置：{key:配置key,value:配置value}
        Map<String, String> allMap = event.getData();

        //  根据bean工厂获得注册工厂
        Map<String, Collection<HolderBeanWrapper>> registry = holderBeanWrapperRegistry.getRegistry(beanFactory);
        if (MapUtils.isEmpty(registry)) {
            log.warn("#refreshBinder skip refreshBinder,registry is empty!");
            return;
        }
        Set<String> keyPrefixSet = new HashSet<>();
        //  循环待刷新的key,获得待刷新的key前缀配置
        for (String key : allMap.keySet()) {
            keyPrefixSet.addAll(
                    registry.keySet()
                            .parallelStream()
                            .filter(bindPrefix -> key.startsWith(bindPrefix))
                            .collect(Collectors.toList())
            );
        }
        Binder binder = Binder.get(environment);
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
                        log.warn("#refreshBinder binder failed: ", ex);
                    }
                }
            }
        }
    }
}
