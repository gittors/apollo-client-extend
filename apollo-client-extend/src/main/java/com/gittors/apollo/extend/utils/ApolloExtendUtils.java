package com.gittors.apollo.extend.utils;

import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.ConfigChangeListener;
import com.ctrip.framework.apollo.ConfigService;
import com.ctrip.framework.apollo.spring.config.ConfigPropertySource;
import com.ctrip.framework.apollo.spring.config.PropertySourcesConstants;
import com.ctrip.framework.apollo.spring.property.AutoUpdateConfigChangeListener;
import com.gittors.apollo.extend.callback.ApolloExtendCallback;
import com.gittors.apollo.extend.common.constant.CommonApolloConstant;
import com.gittors.apollo.extend.common.enums.ChangeType;
import com.gittors.apollo.extend.common.internals.ApolloExtendListenerInjector;
import com.gittors.apollo.extend.common.spi.ServiceLookUp;
import com.gittors.apollo.extend.context.ApolloExtendContext;
import com.gittors.apollo.extend.properties.ApolloExtendGlobalListenKeyProperties;
import com.gittors.apollo.extend.properties.ApolloExtendListenKeyProperties;
import com.gittors.apollo.extend.properties.ApolloExtendPropertySourceProperties;
import com.gittors.apollo.extend.support.ApolloExtendStringMapEntry;
import com.gittors.apollo.extend.support.ext.DefaultConfigExt;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.core.env.ConfigurableEnvironment;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author zlliu
 * @date 2020/1/8 11:29
 */
@Slf4j
public final class ApolloExtendUtils {
    private ApolloExtendUtils() {
    }

    public static void addListener(String namespaces) {
        addListener(namespaces, ApolloExtendContext.INSTANCE.getCallbackMap());
    }

    public static ConfigChangeListener getChangeListener(Map<String, ApolloExtendCallback> callbackMap) {
        return changeEvent -> {
            log.info("ApolloConfig onchange... namespace: {}", changeEvent.getNamespace());
            long changeTimestamp = System.currentTimeMillis();
            for (String key : changeEvent.changedKeys()) {
                // 精确匹配和模糊查询
                for (Map.Entry<String, ApolloExtendCallback> entry : callbackMap.entrySet()) {
                    if (key.equals(entry.getKey()) ||
                            (entry.getKey().indexOf("*") > 0 && key.indexOf(entry.getKey().replace("*","")) >= 0)) {
                        entry.getValue().callback(changeEvent.getNamespace(), changeEvent.getChange(key).getOldValue(), changeEvent.getChange(key).getNewValue(), changeTimestamp);
                    }
                }
            }
        };
    }

    /**
     * 增加Apollo监听器
     * @param namespaces
     * @param callbackMap
     */
    public static void addListener(String namespaces, Map<String, ApolloExtendCallback> callbackMap) {
        Config config = ConfigService.getConfig(namespaces);
        if (config != null) {
            config.addChangeListener(getChangeListener(callbackMap));
        }
    }

    /**
     * 注入自定义监听器、自动绑定监听器等
     * @param configPropertySource
     * @param environment
     * @param beanFactory
     */
    public static void addListener(ConfigPropertySource configPropertySource,
                                   ConfigurableEnvironment environment, BeanFactory beanFactory) {
        if (configPropertySource != null) {
            configPropertySource.addChangeListener(getChangeListener(ApolloExtendContext.INSTANCE.getCallbackMap()));

            List<ConfigChangeListener> changeListenerList = Lists.newLinkedList();
            Iterator<ApolloExtendListenerInjector> list = ServiceLookUp.loadAll(ApolloExtendListenerInjector.class);
            for (Iterator<ApolloExtendListenerInjector> iterator = list; list.hasNext();) {
                ApolloExtendListenerInjector apolloExtendListenerInjector = iterator.next();
                apolloExtendListenerInjector.injector(changeListenerList, environment, beanFactory);
            }

            for (ConfigChangeListener changeListener : changeListenerList) {
                configPropertySource.addChangeListener(changeListener);
            }
        }
    }

    /**
     * 新增自动更新监听器
     * @param configPropertySource
     * @param environment
     * @param beanFactory
     */
    public static void addAutoUpdateListener(ConfigPropertySource configPropertySource,
                                              ConfigurableEnvironment environment, BeanFactory beanFactory) {
        if (configPropertySource != null) {
            AutoUpdateConfigChangeListener autoUpdateConfigChangeListener = new AutoUpdateConfigChangeListener(
                    environment, (ConfigurableListableBeanFactory) beanFactory);
            configPropertySource.addChangeListener(autoUpdateConfigChangeListener);
        }
    }

    /**
     * 查找 listener
     * @param listeners
     * @param predicate
     * @return
     */
    public static List<ConfigChangeListener> findConfigChangeListener(List<ConfigChangeListener> listeners, Predicate<ConfigChangeListener> predicate) {
        List<ConfigChangeListener> changeListenerList =
                listeners.stream()
                        .filter(listener -> predicate.test(listener))
                        .collect(Collectors.toList());
        return CollectionUtils.isNotEmpty(changeListenerList) ? changeListenerList : Lists.newArrayList();
    }

    /**
     * 根据配置前缀拼接 propertySource 名称
     * @param environment
     * @param configPrefix
     * @param namespaceName
     * @return
     */
    public static String getPropertySourceName(ConfigurableEnvironment environment, String configPrefix, String namespaceName) {
        ApolloExtendPropertySourceProperties propertySourceConfig =
                Binder.get(environment)
                        .bind(configPrefix, Bindable.of(ApolloExtendPropertySourceProperties.class))
                        .orElse(new ApolloExtendPropertySourceProperties());
        String propertySourceCnfSuffix = propertySourceConfig.getPropertyMap().get(namespaceName);
        String propertySourceSuffix = StringUtils.isNotBlank(propertySourceCnfSuffix) ? propertySourceCnfSuffix : PropertySourcesConstants.APOLLO_PROPERTY_SOURCE_NAME;
        return ApolloExtendStringUtils.format(namespaceName, null, propertySourceSuffix);
    }

    /**
     * 匹配 Key是否存在于监听Key集合
     * @param key   参数Key
     * @return
     */
    public static boolean predicateMatch(String key, Map.Entry<Boolean, Set<String>> configEntry) {
        if (!configEntry.getKey() || CollectionUtils.isEmpty(configEntry.getValue())) {
            return true;
        }
        boolean match = false;
        for (String prefix : configEntry.getValue()) {
            if (key.startsWith(prefix)) {
                match = true;
                break;
            }
        }
        return match;
    }

    /**
     * 构建跳过验证的Key
     * @return
     */
    public static Map.Entry<Boolean, Set<String>> skipMatchConfig() {
        Set<String> skipMatchConfig = Sets.newHashSet();
        skipMatchConfig.add(CommonApolloConstant.PROPERTY_SOURCE_CONFIG_DEFAULT_SUFFIX);
        skipMatchConfig.add(CommonApolloConstant.APOLLO_EXTEND_LISTEN_KEY_SUFFIX);

        return new AbstractMap.SimpleEntry<>(Boolean.TRUE, skipMatchConfig);
    }

    /**
     * 获得命名空间的监听配置
     * @param environment
     * @param namespaceSet
     * @param changeType    {@link ChangeType}
     * @return  {Key: 命名空间，Value：命名空间管理Key集合}
     */
    public static Map<String, Map.Entry<Boolean, Set<String>>> getManagerConfig(ConfigurableEnvironment environment,
                                                                   Set<String> namespaceSet, ChangeType changeType) {
        ApolloExtendGlobalListenKeyProperties globalListenKeyProperties =
                Binder.get(environment)
                        .bind(CommonApolloConstant.APOLLO_EXTEND_GLOBAL_LISTEN_KEY_SUFFIX, Bindable.of(ApolloExtendGlobalListenKeyProperties.class))
                        .orElse(new ApolloExtendGlobalListenKeyProperties());
        ApolloExtendListenKeyProperties listenKeyProperties =
                Binder.get(environment)
                        .bind(CommonApolloConstant.APOLLO_EXTEND_LISTEN_KEY_SUFFIX, Bindable.of(ApolloExtendListenKeyProperties.class))
                        .orElse(new ApolloExtendListenKeyProperties());

        Map<String, Set<String>> mergeMap = globalListenKeyProperties.merge(listenKeyProperties, changeType);
        Map<String, Map.Entry<Boolean, Set<String>>> map = Maps.newHashMap();
        for (String namespace : namespaceSet) {
            Set<String> listenKeyAll =
                    mergeMap.entrySet()
                            .stream()
                            .filter(entry -> namespace.equalsIgnoreCase(entry.getKey()))
                            .map(Map.Entry::getValue)
                            .flatMap(Collection::stream)
                            .collect(Collectors.toSet());
            Map.Entry<Boolean, Set<String>> managerConfigPrefix;
            if (CollectionUtils.isNotEmpty(listenKeyAll)) {
                managerConfigPrefix = new AbstractMap.SimpleEntry<>(Boolean.TRUE, listenKeyAll);
            } else {
                managerConfigPrefix = new AbstractMap.SimpleEntry<>(Boolean.FALSE, null);
            }
            map.put(namespace, managerConfigPrefix);
        }
        return map;
    }

    /**
     * 管理配置部分生效处理
     * @param propertySource
     * @param configEntry
     */
    public static void managerConfigHandler(ConfigPropertySource propertySource, Map.Entry<Boolean, Set<String>> configEntry) {
        //  如果是FALSE全部生效，就不用设置回调
        if (configEntry.getKey()) {
            DefaultConfigExt defaultConfig = (DefaultConfigExt) propertySource.getSource();

            Properties properties = new Properties();
            //  1根据配置监听Key 筛选生效属性
            defaultConfig.getPropertyNames()
                    .stream()
                    .filter(configKey -> ApolloExtendUtils.predicateMatch(configKey, configEntry) || ApolloExtendUtils.predicateMatch(configKey, ApolloExtendUtils.skipMatchConfig()))
                    .forEach(configKey -> properties.setProperty(configKey, defaultConfig.getProperty(configKey, "")));

            //  2刷新对象
            defaultConfig.updateConfig(properties, propertySource.getSource().getSourceType());

            //  3设置回调
            defaultConfig.addPropertiesCallBack(updateProperties -> {
                Properties filterProperties = new Properties();
                Properties property = (Properties) updateProperties;
                property.entrySet().stream()
                        .map(objectEntry -> new ApolloExtendStringMapEntry(String.valueOf(objectEntry.getKey()), String.valueOf(objectEntry.getValue())))
                        .filter(stringEntry -> ApolloExtendUtils.predicateMatch(stringEntry.getKey(), configEntry) || ApolloExtendUtils.predicateMatch(stringEntry.getKey(), ApolloExtendUtils.skipMatchConfig()))
                        .forEach(stringEntry -> filterProperties.setProperty(stringEntry.getKey(), stringEntry.getValue()));
                return filterProperties;
            });
        }
    }

}
