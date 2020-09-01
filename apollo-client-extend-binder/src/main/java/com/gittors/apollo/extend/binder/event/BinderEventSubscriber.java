package com.gittors.apollo.extend.binder.event;

import com.gittors.apollo.extend.binder.registry.HolderBeanWrapper;
import com.gittors.apollo.extend.binder.registry.HolderBeanWrapperRegistry;
import com.gittors.apollo.extend.binder.utils.BinderUtils;
import com.gittors.apollo.extend.binder.utils.BinderObjectInjector;
import com.google.common.eventbus.Subscribe;
import com.nepxion.eventbus.annotation.EventBus;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.core.env.Environment;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
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
        Map<String, List<Map<String, String>>> data = event.getData();
        List<Map<String, String>> list = data.entrySet()
                .stream()
                .map(entry -> entry.getValue())
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        Map<String, Collection<HolderBeanWrapper>> registry = holderBeanWrapperRegistry.getRegistry(beanFactory);
        if (MapUtils.isEmpty(registry)) {
            return;
        }

        //  list 代表多个配置文件的配置项【客户端监听的配置前缀筛选后的结果】
        for (Map<String, String> map : list) {
            Set<String> keySet = new HashSet<>();
            for (String key : map.keySet()) {
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
}
