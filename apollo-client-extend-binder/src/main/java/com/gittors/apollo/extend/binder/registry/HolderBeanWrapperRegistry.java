package com.gittors.apollo.extend.binder.registry;

import com.ctrip.framework.apollo.core.utils.ApolloThreadFactory;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import org.springframework.beans.factory.BeanFactory;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author zlliu
 * @date 2020/8/10 21:39
 */
public class HolderBeanWrapperRegistry {
    /**
     * 清理间隔：5秒
     */
    private static final long CLEAN_INTERVAL_IN_SECONDS = 5;

    private final Map<BeanFactory, Multimap<String, HolderBeanWrapper>> registry = Maps.newConcurrentMap();

    private final AtomicBoolean initialized = new AtomicBoolean(false);

    private final Object LOCK = new Object();

    public void register(BeanFactory beanFactory, String key, HolderBeanWrapper holderBeanWrapper) {
        if (!registry.containsKey(beanFactory)) {
            synchronized (LOCK) {
                if (!registry.containsKey(beanFactory)) {
                    registry.put(beanFactory, Multimaps.synchronizedListMultimap(LinkedListMultimap.<String, HolderBeanWrapper>create()));
                }
            }
        }

        registry.get(beanFactory).put(key, holderBeanWrapper);

        // lazy initialize
        if (initialized.compareAndSet(false, true)) {
            initialize();
        }
    }

    public Map<String, Collection<HolderBeanWrapper>> getRegistry(BeanFactory beanFactory) {
        Multimap<String, HolderBeanWrapper> holderBeanWrapperMultimap = registry.get(beanFactory);
        if (holderBeanWrapperMultimap != null) {
            return holderBeanWrapperMultimap.asMap();
        }
        return Maps.newHashMap();
    }

    public Collection<HolderBeanWrapper> get(BeanFactory beanFactory, String key) {
        Multimap<String, HolderBeanWrapper> holderBeanWrapperMultimap = registry.get(beanFactory);
        if (holderBeanWrapperMultimap == null) {
            return null;
        }
        return holderBeanWrapperMultimap.get(key);
    }

    private void initialize() {
        Executors.newSingleThreadScheduledExecutor(
                ApolloThreadFactory.create("ApolloExtend-ConfigurationPropertiesRegistry", true)
        ).scheduleAtFixedRate(
                () -> {
                    try {
                        scanAndClean();
                    } catch (Throwable ex) {
                        ex.printStackTrace();
                    }
                }, CLEAN_INTERVAL_IN_SECONDS, CLEAN_INTERVAL_IN_SECONDS, TimeUnit.SECONDS);
    }

    /**
     * 定期移除失效的bean
     */
    private void scanAndClean() {
        Iterator<Multimap<String, HolderBeanWrapper>> iterator = registry.values().iterator();
        while (!Thread.currentThread().isInterrupted() && iterator.hasNext()) {
            Multimap<String, HolderBeanWrapper> wrapperMultimap = iterator.next();
            Iterator<Entry<String, HolderBeanWrapper>> holdBeanWrapperWrapperIterator = wrapperMultimap.entries().iterator();
            while (holdBeanWrapperWrapperIterator.hasNext()) {
                Entry<String, HolderBeanWrapper> holdBeanWrapperEntry = holdBeanWrapperWrapperIterator.next();
                if (!holdBeanWrapperEntry.getValue().isTargetBeanValid()) {
                    // clear unused spring values
                    holdBeanWrapperWrapperIterator.remove();
                }
            }
        }
    }
}
