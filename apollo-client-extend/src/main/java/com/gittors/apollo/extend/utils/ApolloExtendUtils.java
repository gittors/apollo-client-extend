package com.gittors.apollo.extend.utils;

import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.ConfigChangeListener;
import com.ctrip.framework.apollo.ConfigService;
import com.ctrip.framework.apollo.core.ConfigConsts;
import com.ctrip.framework.apollo.spring.config.PropertySourcesConstants;
import com.ctrip.framework.apollo.spring.property.AutoUpdateConfigChangeListener;
import com.gittors.apollo.extend.callback.ApolloExtendCallback;
import com.gittors.apollo.extend.common.constant.CommonApolloConstant;
import com.gittors.apollo.extend.common.context.ApolloPropertySourceContext;
import com.gittors.apollo.extend.common.enums.ChangeType;
import com.gittors.apollo.extend.common.env.SimplePropertySource;
import com.gittors.apollo.extend.common.service.ServiceLookUp;
import com.gittors.apollo.extend.common.spi.ApolloExtendListenerInjector;
import com.gittors.apollo.extend.context.ApolloExtendContext;
import com.gittors.apollo.extend.env.SimpleCompositePropertySource;
import com.gittors.apollo.extend.properties.ApolloExtendGlobalListenKeyProperties;
import com.gittors.apollo.extend.properties.ApolloExtendListenKeyProperties;
import com.gittors.apollo.extend.spi.ApolloConfigChangeCallBack;
import com.gittors.apollo.extend.support.ApolloExtendFactory;
import com.gittors.apollo.extend.support.ext.ApolloClientExtendConfig;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertySources;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.StandardEnvironment;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author zlliu
 * @date 2020/1/8 11:29
 */
@Slf4j
public final class ApolloExtendUtils {
    private static final ApolloConfigChangeCallBack apolloConfigChangeCallBack =
            ServiceLookUp.loadPrimary(ApolloConfigChangeCallBack.class);

    private static final Splitter NAMESPACE_SPLITTER = Splitter.on(CommonApolloConstant.DEFAULT_SEPARATOR)
            .omitEmptyStrings().trimResults();

    private ApolloExtendUtils() {
    }

    public static void addListener(String namespaces) {
        addListener(namespaces, ApolloExtendContext.INSTANCE.getCallbackMap());
    }

    /**
     * 增加Apollo监听器
     * @param namespace
     * @param callbackMap
     */
    public static void addListener(String namespace, Map<String, ApolloExtendCallback> callbackMap) {
        Config config = ConfigService.getConfig(namespace);
        if (config != null) {
            config.addChangeListener(getChangeListener(callbackMap));
        }
    }

    public static ConfigChangeListener getChangeListener(Map<String, ApolloExtendCallback> callbackMap) {
        return changeEvent -> {
            log.info("ApolloConfig onchange... namespace: {}", changeEvent.getNamespace());
            if (apolloConfigChangeCallBack != null) {
                apolloConfigChangeCallBack.callBack(callbackMap, changeEvent);
            }
        };
    }

    /**
     * 注入监听器:
     * 1.自动更新监听器
     * 2.自定义监听器
     * 3.自动绑定监听器等
     * @param configPropertySource
     * @param environment
     * @param beanFactory
     */
    public static void addListener(SimplePropertySource configPropertySource,
                                   ConfigurableEnvironment environment, ConfigurableListableBeanFactory beanFactory) {
        if (configPropertySource != null) {
            configPropertySource.addChangeListener(new AutoUpdateConfigChangeListener(environment, beanFactory));
            configPropertySource.addChangeListener(getChangeListener(ApolloExtendContext.INSTANCE.getCallbackMap()));

            List<ConfigChangeListener> changeListenerList = Lists.newLinkedList();
            Iterator<ApolloExtendListenerInjector> loadAll = ServiceLookUp.loadAll(ApolloExtendListenerInjector.class);
            for (Iterator<ApolloExtendListenerInjector> iterator = loadAll; iterator.hasNext();) {
                ApolloExtendListenerInjector listenerInjector = iterator.next();
                listenerInjector.injector(changeListenerList, environment, beanFactory);
            }

            for (ConfigChangeListener changeListener : changeListenerList) {
                configPropertySource.addChangeListener(changeListener);
            }
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
     * @param namespaceName 命名空间名称
     * @return
     */
    public static String getPropertySourceName(String namespaceName) {
        return ApolloExtendStringUtils.format(namespaceName, null, CommonApolloConstant.APOLLO_EXTEND_PROPERTY_SOURCE_NAME);
    }

    /**
     * 匹配 Key是否存在于监听Key集合
     * @param key   参数Key，如：my.map.name3
     * @param configEntry   管理配置 listen.key.addMap 的key：如 my.map
     * @return
     */
    public static boolean predicateMatch(String key, Map.Entry<Boolean, Set<String>> configEntry) {
        //  FALSE 代表没有找到管理配置, 不用过滤
        if (!configEntry.getKey() || CollectionUtils.isEmpty(configEntry.getValue())) {
            return true;
        }
        return configEntry.getValue().stream()
                .filter(StringUtils::isNotBlank)
                .anyMatch(prefix -> key.startsWith(prefix));
    }

    /**
     * 构建跳过验证的 KEY
     * @return
     */
    public static Map.Entry<Boolean, Set<String>> skipMatchConfig() {
        Set<String> skipMatchConfig = Sets.newHashSet();
        skipMatchConfig.add(CommonApolloConstant.APOLLO_EXTEND_LISTEN_KEY_SUFFIX);
        skipMatchConfig.add(CommonApolloConstant.APOLLO_EXTEND_NAMESPACE);

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
        Binder binder = Binder.get(environment);
        //  全局配置项：listen.key.global.map.application2 = my.map
        ApolloExtendGlobalListenKeyProperties globalListenKeyProperties =
                binder.bind(CommonApolloConstant.APOLLO_EXTEND_GLOBAL_LISTEN_KEY_SUFFIX, Bindable.of(ApolloExtendGlobalListenKeyProperties.class))
                        .orElse(new ApolloExtendGlobalListenKeyProperties());
        //  局部配置项：listen.key.addMap.application2 = my.map2
        ApolloExtendListenKeyProperties listenKeyProperties =
                binder.bind(CommonApolloConstant.APOLLO_EXTEND_LISTEN_KEY_SUFFIX, Bindable.of(ApolloExtendListenKeyProperties.class))
                        .orElse(new ApolloExtendListenKeyProperties());

        //  合并全局+局部配置项：{key:application2,value:[my.map,my.map2]}
        Map<String, Set<String>> mergeMap = globalListenKeyProperties.merge(listenKeyProperties, changeType);
        Map<String, Map.Entry<Boolean, Set<String>>> map = Maps.newHashMap();
        for (String namespace : namespaceSet) {
            //  mergeMap是合并后的所有管理配置项
            //  这个过滤的结果为：找出匹配namespaceSet范围内的命名空间配置项
            Set<String> listenKeyAll = mergeMap.entrySet().stream()
                    .filter(entry -> namespace.equalsIgnoreCase(entry.getKey()))
                    .map(Map.Entry::getValue)
                    .flatMap(Collection::stream)
                    .filter(StringUtils::isNotBlank)
                    .collect(Collectors.toSet());
            Map.Entry<Boolean, Set<String>> managerConfigPrefix;
            if (CollectionUtils.isNotEmpty(listenKeyAll)) {
                managerConfigPrefix = new AbstractMap.SimpleEntry<>(Boolean.TRUE, listenKeyAll);
            } else {
                //  没有找到管理配置
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
    public static void configValidHandler(SimplePropertySource propertySource, Map.Entry<Boolean, Set<String>> configEntry,
                                          ApolloExtendFactory.PropertyFilterPredicate filterPredicate) {
        //  利用ConfigRepository的 m_fileProperties 配置没问题：因为如果远端Apollo配置更新，会先更新 LocalFileConfigRepository 的 m_fileProperties
        //  参考：com.ctrip.framework.apollo.internals.LocalFileConfigRepository.onRepositoryChange
        Properties sourceProperties = ((ApolloClientExtendConfig) propertySource.getSource()).getConfigRepository().getConfig();
        //  如果是FALSE全部生效，不用设置回调
        if (configEntry.getKey() && validConfigKey(sourceProperties, configEntry.getValue())) {
            Properties properties = new Properties();
            //  1.根据配置监听Key 筛选生效属性
            sourceProperties.stringPropertyNames()
                    .stream()
                    .filter(configKey -> filterPredicate.match(configKey, configEntry))
                    .forEach(configKey -> properties.setProperty(configKey, sourceProperties.getProperty(configKey, "")));

            ApolloClientExtendConfig defaultConfig = (ApolloClientExtendConfig) propertySource.getSource();
            //  2.刷新对象
            defaultConfig.updateConfig(properties, propertySource.getSource().getSourceType());

            //  3.设置回调
            //  回调的作用是每次Apollo命名空间的配置动态刷新时，利用这个回调过滤掉一些配置：
            //  由于动态失效或生效是利用更新了Apollo的内存对象DefaultConfigExt的原理，真正的Apollo WEB界面这个配置是存在的，所以这个回调显得很重要~!!
            //  比如：设置了某些key失效了【listen.key.delMap.application2 = my.key】，Apollo更新配置发布后，这些失效的key还会带过来，利用这个回调过滤掉即可
            defaultConfig.addPropertiesCallBack(updateProperties -> {
                Properties filterProperties = new Properties();
                Properties property = (Properties) updateProperties;
                property.stringPropertyNames().stream()
                        .filter(configKey -> filterPredicate.match(configKey, configEntry))
                        .forEach(configKey -> filterProperties.setProperty(configKey, property.getProperty(configKey, "")));
                return filterProperties;
            });
        }
    }

    public static ApolloExtendFactory.PropertyFilterPredicate getFilterPredicate(boolean flag) {
        return (key, configEntry) -> {
            //  flag: TRUE- 新增 FALSE-删除
            if (flag) {
                //  满足配置KEY的保留 + 管理配置的KEY保留
                return ApolloExtendUtils.predicateMatch(key, configEntry) ||
                        ApolloExtendUtils.predicateMatch(key, ApolloExtendUtils.skipMatchConfig());
            } else {
                //  不满足配置KEY的保留 + 管理配置的KEY保留
                return !ApolloExtendUtils.predicateMatch(key, configEntry) ||
                        ApolloExtendUtils.predicateMatch(key, ApolloExtendUtils.skipMatchConfig());
            }
        };
    }

    /**
     * 判断管理的KEY是否是有效的
     * @param sourceProperties
     * @param keySet
     * @return FALSE-不是有效的KEY，TRUE-有效的KEY
     */
    private static boolean validConfigKey(Properties sourceProperties, Set<String> keySet) {
        for (String propertyName : sourceProperties.stringPropertyNames()) {
            boolean match = keySet.stream().anyMatch(key -> propertyName.startsWith(key));
            if (match) {
                return true;
            }
        }
        return false;
    }

    public static ApolloExtendFactory.DataFilter getDataFilterPredicate(ConfigurableEnvironment environment) {
        BiPredicate<String, Map.Entry<Boolean, Set<String>>> predicate = null;
        //  没有配置属性过滤开关，默认返回所有KEY; 配置了开关则返回精确匹配的KEY
        if (!environment.getProperty(CommonApolloConstant.APOLLO_PROPERTY_FILTER_ENABLE, Boolean.class, Boolean.FALSE)) {
            predicate = (configKey, configEntry) -> true;
        } else {
            predicate = (configKey, configEntry) -> ApolloExtendUtils.predicateMatch(configKey, configEntry);
        }
        BiPredicate<String, Map.Entry<Boolean, Set<String>>> biPredicate = predicate;
        return (propertySource, configEntry) -> {
            Map<String, String> map = Maps.newHashMap();
            propertySource.getSource()
                    .getPropertyNames()
                    .stream()
                    .filter(configKey -> !ApolloExtendUtils.predicateMatch(configKey, ApolloExtendUtils.skipMatchConfig()))
                    .filter(configKey -> biPredicate.test(configKey, configEntry))
                    .forEach(configKey -> map.put(configKey, propertySource.getSource().getProperty(configKey, "")));
            return map;
        };
    }

    /**
     * 合并环境
     * @param environment
     * @param configPropertySourceList
     * @return
     */
    public static ConfigurableEnvironment mergeEnvironment(ConfigurableEnvironment environment, List<SimplePropertySource> configPropertySourceList) {
        ConfigurableEnvironment standardEnvironment = new StandardEnvironment();
        standardEnvironment.merge(environment);

        configPropertySourceList.forEach(standardEnvironment.getPropertySources()::addLast);

        ConfigurationPropertySources.attach(standardEnvironment);
        return standardEnvironment;
    }

    public static SimpleCompositePropertySource getCompositePropertySource(ConfigurableEnvironment environment) {
        MutablePropertySources mutablePropertySources = environment.getPropertySources();
        SimpleCompositePropertySource bootstrapComposite;
        if (!mutablePropertySources.contains(CommonApolloConstant.APOLLO_BOOTSTRAP_PROPERTY_SOURCE_NAME)) {
            bootstrapComposite = new SimpleCompositePropertySource(CommonApolloConstant.APOLLO_BOOTSTRAP_PROPERTY_SOURCE_NAME);
            mutablePropertySources.addAfter(PropertySourcesConstants.APOLLO_PROPERTY_SOURCE_NAME, bootstrapComposite);
        } else {
            bootstrapComposite = (SimpleCompositePropertySource) mutablePropertySources.get(CommonApolloConstant.APOLLO_BOOTSTRAP_PROPERTY_SOURCE_NAME);
        }
        return bootstrapComposite;
    }

    public static SimplePropertySource findPropertySource(Collection<SimplePropertySource> collection,
                                                          Predicate<SimplePropertySource> predicate) {
        if (CollectionUtils.isEmpty(collection) || predicate == null) {
            return null;
        }
        for (SimplePropertySource propertySource : collection) {
            if (predicate.test(propertySource)) {
                return propertySource;
            }
        }
        return null;
    }

    public static ApolloExtendFactory.PropertySourceFactory getPropertySourceFactory(String name, String namespace, Config source) {
        return (created, cached, propertySource) -> {
            if (created && cached) {
                SimplePropertySource simplePropertySource = new SimplePropertySource(name, namespace, source);
                ApolloPropertySourceContext.INSTANCE.getPropertySources().add(simplePropertySource);
                return simplePropertySource;
            } else if (created && !cached) {
                return new SimplePropertySource(name, namespace, source);
            } else if (!created && cached) {
                ApolloPropertySourceContext.INSTANCE.getPropertySources().add(propertySource);
                return null;
            } else {
                return null;
            }
        };
    }

    /**
     * 解析配置: ","号分割
     * @param config
     * @return
     */
    public static Set<String> parseNamespace(String config) {
        List<String> namespaceList = NAMESPACE_SPLITTER.splitToList(config);
        return namespaceList.stream()
                .filter(namespace -> !ConfigConsts.NAMESPACE_APPLICATION.equals(namespace))
                .collect(Collectors.toSet());
    }

}
