package com.gittors.apollo.extend.spi;

import com.ctrip.framework.apollo.ConfigChangeListener;
import com.ctrip.framework.apollo.ConfigService;
import com.ctrip.framework.apollo.core.ConfigConsts;
import com.ctrip.framework.apollo.enums.ConfigSourceType;
import com.ctrip.framework.apollo.spring.config.ConfigPropertySource;
import com.ctrip.framework.apollo.spring.config.ConfigPropertySourceFactory;
import com.ctrip.framework.apollo.spring.util.SpringInjector;
import com.gittors.apollo.extend.common.constant.CommonApolloConstant;
import com.gittors.apollo.extend.common.constant.CommonConstant;
import com.gittors.apollo.extend.common.enums.ChangeType;
import com.gittors.apollo.extend.common.spi.Ordered;
import com.gittors.apollo.extend.common.utils.ListUtils;
import com.gittors.apollo.extend.properties.ApolloExtendGlobalListenKeyProperties;
import com.gittors.apollo.extend.properties.ApolloExtendListenKeyProperties;
import com.gittors.apollo.extend.support.ApolloExtendStringMapEntry;
import com.gittors.apollo.extend.support.ext.DefaultConfigExt;
import com.gittors.apollo.extend.utils.ApolloExtendStringUtils;
import com.gittors.apollo.extend.utils.ApolloExtendUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author zlliu
 * @date 2020/8/26 10:38
 */
public class DefaultApolloExtendNameSpaceManager implements ApolloExtendNameSpaceManager<ConfigPropertySource> {
    private final ConfigPropertySourceFactory configPropertySourceFactory =
            SpringInjector.getInstance(ConfigPropertySourceFactory.class);

    private ConfigurableEnvironment environment;
    private BeanFactory beanFactory;

    @Override
    public void setEnvironment(ConfigurableEnvironment environment) {
        this.environment = environment;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    @Override
    public List<Map<String, String>> getAddNamespace(Set<String> needAddNamespaceSet) {
        if (CollectionUtils.isEmpty(needAddNamespaceSet)) {
            return Lists.newArrayList();
        }
        Map.Entry<Boolean, Set<String>> managerConfigPrefix = getManagerConfig(needAddNamespaceSet, ChangeType.ADD);

        List<ConfigPropertySource> addPropertySourceList = doGetAddNamespace(needAddNamespaceSet);

        List<Map<String, String>> addConfig = filter(addPropertySourceList, managerConfigPrefix);

        //  新增配置项
        addNamespace(addPropertySourceList, managerConfigPrefix);

        return addConfig;
    }

    @Override
    public List<Map<String, String>> getDeleteNamespace(Set<String> needDeleteNamespaceSet) {
        if (CollectionUtils.isEmpty(needDeleteNamespaceSet)) {
            return Lists.newArrayList();
        }
        Map.Entry<Boolean, Set<String>> managerConfigPrefix = getManagerConfig(needDeleteNamespaceSet, ChangeType.DELETE);

        List<ConfigPropertySource> deletePropertySourceList = doGetDeleteNamespace(needDeleteNamespaceSet);

        //  筛选出需删除的配置项，用于返回
        List<Map<String, String>> deleteConfig = filter(deletePropertySourceList, managerConfigPrefix);

        //  删除配置项
        deleteNamespace(deletePropertySourceList, managerConfigPrefix);

        return deleteConfig;
    }

    @Override
    public void addNamespace(List<ConfigPropertySource> list, Map.Entry<Boolean, Set<String>> managerConfigPrefix) {
        refreshAddEnvironment(list, managerConfigPrefix);
    }

    @Override
    public void deleteNamespace(List<ConfigPropertySource> list, Map.Entry<Boolean, Set<String>> managerConfigPrefix) {
        refreshDelEnvironment(list, managerConfigPrefix);
    }

    protected List<Map<String, String>> filter(List<ConfigPropertySource> propertySourceList, Map.Entry<Boolean, Set<String>> managerConfigPrefix) {
        List<Map<String, String>> propertiesList = Lists.newArrayList();
        if (CollectionUtils.isEmpty(propertySourceList)) {
            return propertiesList;
        }

        for (ConfigPropertySource propertySource : propertySourceList) {
            Map<String, String> map = Maps.newHashMap();
            propertySource.getSource()
                    .getPropertyNames()
                    .stream()
                    .filter(configKey -> ApolloExtendUtils.predicateMatch(configKey, managerConfigPrefix))
                    .forEach(configKey -> map.put(configKey, propertySource.getSource().getProperty(configKey, "")));

            propertiesList.add(map);
        }
        return propertiesList;
    }

    @Deprecated
    protected List<Map<String, String>> convert(List<ConfigPropertySource> configPropertySourceList) {
        List<Map<String, String>> propertiesList = Lists.newArrayList();
        if (CollectionUtils.isEmpty(configPropertySourceList)) {
            return propertiesList;
        }

        for (ConfigPropertySource propertySource : configPropertySourceList) {
            Map<String, String> map = Maps.newHashMap();
            propertySource.getSource()
                    .getPropertyNames()
                    .stream()
                    .forEach(configKey -> map.put(configKey, propertySource.getSource().getProperty(configKey, "")));

            propertiesList.add(map);
        }
        return propertiesList;
    }

    protected Map.Entry<Boolean, Set<String>> getManagerConfig(Set<String> namespaceSet, ChangeType changeType) {
        ApolloExtendGlobalListenKeyProperties globalListenKeyProperties =
                Binder.get(environment)
                        .bind(CommonApolloConstant.APOLLO_EXTEND_GLOBAL_LISTEN_KEY_SUFFIX, Bindable.of(ApolloExtendGlobalListenKeyProperties.class))
                        .orElse(new ApolloExtendGlobalListenKeyProperties());
        ApolloExtendListenKeyProperties listenKeyProperties =
                Binder.get(environment)
                        .bind(CommonApolloConstant.APOLLO_EXTEND_LISTEN_KEY_SUFFIX, Bindable.of(ApolloExtendListenKeyProperties.class))
                        .orElse(new ApolloExtendListenKeyProperties());

        Map<String, Set<String>> mergeMap = globalListenKeyProperties.merge(listenKeyProperties, changeType);
        Set<String> listenKeyAll =
                mergeMap.entrySet()
                        .stream()
                        .filter(entry -> namespaceSet.contains(entry.getKey()))
                        .map(Map.Entry::getValue)
                        .flatMap(Collection::stream)
                        .collect(Collectors.toSet());
        Map.Entry<Boolean, Set<String>> managerConfigPrefix;
        if (CollectionUtils.isNotEmpty(listenKeyAll)) {
            managerConfigPrefix = new AbstractMap.SimpleEntry<>(Boolean.TRUE, listenKeyAll);
        } else {
            managerConfigPrefix = new AbstractMap.SimpleEntry<>(Boolean.FALSE, null);
        }
        return managerConfigPrefix;
    }

    /**
     * 新增配置，刷新环境
     * @param configPropertySourceList  需删除的配置项
     */
    protected void refreshAddEnvironment(List<ConfigPropertySource> configPropertySourceList, Map.Entry<Boolean, Set<String>> managerConfigPrefix) {
        Map.Entry<Boolean, Set<String>> configEntry = managerConfigPrefix;

        MutablePropertySources mutablePropertySources = environment.getPropertySources();
        configPropertySourceList.forEach(propertySource -> {
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

            String configPrefix = environment.getProperty(CommonApolloConstant.PROPERTY_SOURCE_CONFIG_SUFFIX, CommonApolloConstant.PROPERTY_SOURCE_CONFIG_DEFAULT_SUFFIX);

            String propertySourceName = ApolloExtendUtils.getPropertySourceName(environment, configPrefix, propertySource.getName());

            CompositePropertySource composite = new CompositePropertySource(propertySourceName);
            composite.addPropertySource(propertySource);
            mutablePropertySources.addLast(composite);
            //  添加监听器
            ApolloExtendUtils.addAutoUpdateListener(propertySource, environment, beanFactory);
            ApolloExtendUtils.addListener(propertySource, environment, beanFactory);

        });
    }

    /**
     * 删除配置，刷新环境
     * @param configPropertySourceList  需删除的配置项
     */
    protected void refreshDelEnvironment(List<ConfigPropertySource> configPropertySourceList, Map.Entry<Boolean, Set<String>> managerConfigPrefix) {
        //  1、删除 Spring环境配置
        //  2、根据监听Key，删除配置
        //  3、刷新对象、移除监听器、设置回调
        //  4、刷新 Spring环境配置
        Map.Entry<Boolean, Set<String>> configEntry = managerConfigPrefix;
        MutablePropertySources mutablePropertySources = environment.getPropertySources();

        configPropertySourceList.forEach(propertySource -> {
            //  1.1删除 Spring环境配置
            String configPrefix = environment.getProperty(CommonApolloConstant.PROPERTY_SOURCE_CONFIG_SUFFIX, CommonApolloConstant.PROPERTY_SOURCE_CONFIG_DEFAULT_SUFFIX);

            String propertySourceName = ApolloExtendUtils.getPropertySourceName(environment, configPrefix, propertySource.getName());
            mutablePropertySources.remove(propertySourceName);
            mutablePropertySources.remove(ApolloExtendStringUtils.format(propertySourceName, null, CommonConstant.PROPERTY_NAME_SUFFIX));

            Properties properties = new Properties();
            //  2.1删除 "监听前缀" 的配置，管理配置除外
            propertySource.getSource()
                    .getPropertyNames()
                    .stream()
                    .filter(configKey -> !ApolloExtendUtils.predicateMatch(configKey, configEntry) || ApolloExtendUtils.predicateMatch(configKey, ApolloExtendUtils.skipMatchConfig()))
                    .forEach(configKey -> properties.setProperty(configKey, propertySource.getSource().getProperty(configKey, "")));

            //  3.1刷新对象
            DefaultConfigExt defaultConfig = ((DefaultConfigExt) propertySource.getSource());
            defaultConfig.updateConfig(properties, propertySource.getSource().getSourceType());

            //  3.2移除相关监听器【废弃】

            //  3.3设置配置回调，管理配置除外
            defaultConfig.addPropertiesCallBack(updateProperties -> {
                Properties filterProperties = new Properties();
                Properties property = (Properties) updateProperties;
                property.entrySet().stream()
                        .map(objectEntry -> new ApolloExtendStringMapEntry(String.valueOf(objectEntry.getKey()), String.valueOf(objectEntry.getValue())))
                        .filter(stringEntry -> !ApolloExtendUtils.predicateMatch(stringEntry.getKey(), configEntry) || ApolloExtendUtils.predicateMatch(stringEntry.getKey(), ApolloExtendUtils.skipMatchConfig()))
                        .forEach(stringEntry -> filterProperties.setProperty(stringEntry.getKey(), stringEntry.getValue()));
                return filterProperties;
            });

            //  4.1刷新 Spring环境配置
            CompositePropertySource composite = new CompositePropertySource(ApolloExtendStringUtils.format(propertySourceName, null, CommonConstant.PROPERTY_NAME_SUFFIX));
            composite.addPropertySource(propertySource);
            mutablePropertySources.addLast(composite);
        });
    }

    /**
     * 返回需新增的配置
     * @param needAddNamespaceSet
     */
    protected List<ConfigPropertySource> doGetAddNamespace(Set<String> needAddNamespaceSet) {
        MutablePropertySources mutablePropertySources = environment.getPropertySources();
        String configPrefix = environment.getProperty(CommonApolloConstant.PROPERTY_SOURCE_CONFIG_SUFFIX, CommonApolloConstant.PROPERTY_SOURCE_CONFIG_DEFAULT_SUFFIX);
        List<ConfigPropertySource> addPropertySourceList = Lists.newArrayList();
        for (String routeNamespace : needAddNamespaceSet) {
            String propertySourceName = ApolloExtendUtils.getPropertySourceName(environment, configPrefix, routeNamespace);

            //  不存在有两种情况：
            //  1、新增的：直接新增
            //  2、删除有添加的：刷新对象，重新添加
            if (!mutablePropertySources.contains(propertySourceName)) {
                ConfigPropertySource configPropertySource = null;
                String backUpPropertySourceName = ApolloExtendStringUtils.format(propertySourceName, null, CommonConstant.PROPERTY_NAME_SUFFIX);
                //  有备份，说明之前删除过
                if (mutablePropertySources.contains(backUpPropertySourceName)) {
                    CompositePropertySource backUpComposite = (CompositePropertySource) mutablePropertySources.remove(backUpPropertySourceName);
                    Optional<PropertySource<?>> optional = backUpComposite.getPropertySources().stream()
                            .filter(propertySource -> propertySource.getSource() != null)
                            .findAny();
                    configPropertySource = optional.isPresent() ? (ConfigPropertySource) optional.get() : null;
                    if (configPropertySource == null) {
                        configPropertySource = configPropertySourceFactory.getConfigPropertySource(routeNamespace, ConfigService.getConfig(routeNamespace));
                    } else {
                        DefaultConfigExt defaultConfig = ((DefaultConfigExt) configPropertySource.getSource());
                        //  删除 listener
                        for (ConfigChangeListener changeListener : defaultConfig.getChangeListener()) {
                            if (changeListener != null) {
                                defaultConfig.removeChangeListener(changeListener);
                            }
                        }

                        defaultConfig.initialize();
                    }
                } else {
                    configPropertySource = configPropertySourceFactory.getConfigPropertySource(routeNamespace, ConfigService.getConfig(routeNamespace));
                }
                //  Load failed skip
                if (configPropertySource.getSource() == null ||
                        configPropertySource.getSource().getSourceType() == ConfigSourceType.NONE) {
                    continue;
                }
                addPropertySourceList.add(configPropertySource);
            }
        }
        return addPropertySourceList;
    }

    /**
     * 根据配置的 "监听属性" 匹配需要移除的 命名空间
     *
     * @param needDeleteNamespaceSet
     * @return
     */
    protected List<ConfigPropertySource> doGetDeleteNamespace(Set<String> needDeleteNamespaceSet) {
        if (CollectionUtils.isEmpty(needDeleteNamespaceSet)) {
            return Lists.newArrayList();
        }
        //  1、去重
        //  2、排除application
        //  3、根据 "需要删除的命名空间" namespaceSet 筛选
        //  4、判断是否包涵 "指定前缀" 的配置
        Stream<ConfigPropertySource> stream = configPropertySourceFactory.getAllConfigPropertySources().stream()
                .filter(configPropertySource -> configPropertySource.getSource().getSourceType() != ConfigSourceType.NONE)
                .filter(ListUtils.distinctByKey(PropertySource::getName))
                .filter(configPropertySource -> !ConfigConsts.NAMESPACE_APPLICATION.equalsIgnoreCase(configPropertySource.getName()))
                .filter(configPropertySource -> needDeleteNamespaceSet.contains(configPropertySource.getName()));
        List<ConfigPropertySource> list = stream.collect(Collectors.toList());
        return CollectionUtils.isEmpty(list) ? Lists.newArrayList() : list;
    }


    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
