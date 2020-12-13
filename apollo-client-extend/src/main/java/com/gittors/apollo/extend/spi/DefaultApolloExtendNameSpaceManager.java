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
import com.gittors.apollo.extend.support.ApolloExtendStringMapEntry;
import com.gittors.apollo.extend.support.ext.DefaultConfigExt;
import com.gittors.apollo.extend.utils.ApolloExtendStringUtils;
import com.gittors.apollo.extend.utils.ApolloExtendUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 这个缺省的实现：
 * Apollo的管理配置：{@link CommonApolloConstant#APOLLO_EXTEND_NAMESPACE}
 * 的新增和删除的处理类
 *
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
    public Map<String, Map<String, String>> getAddNamespace(Set<String> needAddNamespaceSet) {
        if (CollectionUtils.isEmpty(needAddNamespaceSet)) {
            return Maps.newHashMap();
        }
        //  获得合并的配置项
        Map<String, Map.Entry<Boolean, Set<String>>> managerConfigMap = ApolloExtendUtils.getManagerConfig(environment, needAddNamespaceSet, ChangeType.ADD);

        //  获得新增命名空间的propertySource
        List<ConfigPropertySource> addPropertySourceList = doGetAddNamespace(needAddNamespaceSet);

        //  过滤掉"apollo extend管理配置"之外的配置项【用户真正需要关心的配置项】
        Map<String, Map<String, String>> addConfig = filter(addPropertySourceList, managerConfigMap);

        //  新增配置项，刷新spring环境
        addNamespace(addPropertySourceList, managerConfigMap);

        return addConfig;
    }

    @Override
    public Map<String, Map<String, String>> getDeleteNamespace(Set<String> needDeleteNamespaceSet) {
        if (CollectionUtils.isEmpty(needDeleteNamespaceSet)) {
            return Maps.newHashMap();
        }
        //  获得合并的配置项
        Map<String, Map.Entry<Boolean, Set<String>>> managerConfigMap = ApolloExtendUtils.getManagerConfig(environment, needDeleteNamespaceSet, ChangeType.DELETE);

        //  获得需删除的命名空间的propertySource
        List<ConfigPropertySource> deletePropertySourceList = doGetDeleteNamespace(needDeleteNamespaceSet);

        //  过滤掉"apollo extend管理配置"之外的配置项【用户真正需要关心的配置项】
        //  筛选出需删除的配置项，用于返回
        Map<String, Map<String, String>> deleteConfig = filter(deletePropertySourceList, managerConfigMap);

        //  删除配置项，刷新spring环境
        deleteNamespace(deletePropertySourceList, managerConfigMap);

        return deleteConfig;
    }

    @Override
    public void addNamespace(List<ConfigPropertySource> list, Map<String, Map.Entry<Boolean, Set<String>>> managerConfigMap) {
        refreshAddEnvironment(list, managerConfigMap);
    }

    @Override
    public void deleteNamespace(List<ConfigPropertySource> list, Map<String, Map.Entry<Boolean, Set<String>>> managerConfigMap) {
        refreshDelEnvironment(list, managerConfigMap);
    }

    protected
    Map<String, Map<String, String>> filter(List<ConfigPropertySource> propertySourceList, Map<String, Map.Entry<Boolean, Set<String>>> managerConfigMap) {
        Map<String, Map<String, String>> propertiesMap = Maps.newHashMap();
        if (CollectionUtils.isEmpty(propertySourceList)) {
            return propertiesMap;
        }

        for (ConfigPropertySource propertySource : propertySourceList) {
            Map<String, String> map = Maps.newHashMap();
            propertySource.getSource()
                    .getPropertyNames()
                    .stream()
                    .filter(configKey -> ApolloExtendUtils.predicateMatch(configKey, managerConfigMap.get(propertySource.getName()))
                            && !ApolloExtendUtils.predicateMatch(configKey, ApolloExtendUtils.skipMatchConfig()))
                    .forEach(configKey -> map.put(configKey, propertySource.getSource().getProperty(configKey, "")));

            propertiesMap.put(propertySource.getName(), map);
        }
        return propertiesMap;
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

    /**
     * 新增配置，刷新环境
     * @param configPropertySourceList  需删除的配置项
     */
    protected void refreshAddEnvironment(List<ConfigPropertySource> configPropertySourceList, Map<String, Map.Entry<Boolean, Set<String>>> managerConfigMap) {
        MutablePropertySources mutablePropertySources = environment.getPropertySources();
        configPropertySourceList.forEach(propertySource -> {
            ApolloExtendUtils.managerConfigHandler(propertySource, managerConfigMap.get(propertySource.getName()));

            //  获得命名空间对应的spring propertySource前缀名称
            String configPrefix = environment.getProperty(CommonApolloConstant.PROPERTY_SOURCE_CONFIG_SUFFIX, CommonApolloConstant.PROPERTY_SOURCE_CONFIG_DEFAULT_SUFFIX);

            //  获得命名空间对应的spring propertySource名称
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
     *
     * 注意：要删除的配置是在已生效的配置基础上删除的
     *
     * @param configPropertySourceList  需删除的配置项
     */
    protected void refreshDelEnvironment(List<ConfigPropertySource> configPropertySourceList, Map<String, Map.Entry<Boolean, Set<String>>> managerConfigMap) {
        //  1、删除 Spring环境配置
        //  2、根据监听Key，删除配置
        //  3、刷新对象、移除监听器、设置回调
        //  4、刷新 Spring环境配置
        MutablePropertySources mutablePropertySources = environment.getPropertySources();

        configPropertySourceList.forEach(propertySource -> {
            //  1.1删除 Spring环境配置
            //  获得命名空间对应的spring propertySource前缀名称
            String configPrefix = environment.getProperty(CommonApolloConstant.PROPERTY_SOURCE_CONFIG_SUFFIX, CommonApolloConstant.PROPERTY_SOURCE_CONFIG_DEFAULT_SUFFIX);

            //  获得命名空间对应的spring propertySource名称
            String propertySourceName = ApolloExtendUtils.getPropertySourceName(environment, configPrefix, propertySource.getName());
            mutablePropertySources.remove(propertySourceName);
            mutablePropertySources.remove(ApolloExtendStringUtils.format(propertySourceName, null, CommonConstant.PROPERTY_NAME_SUFFIX));

            Properties properties = new Properties();
            //  2.1删除 "监听前缀" 的配置，管理配置除外
            propertySource.getSource()
                    .getPropertyNames()
                    .stream()
                    .filter(configKey -> !ApolloExtendUtils.predicateMatch(configKey, managerConfigMap.get(propertySource.getName())) || ApolloExtendUtils.predicateMatch(configKey, ApolloExtendUtils.skipMatchConfig()))
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
                        .filter(stringEntry -> !ApolloExtendUtils.predicateMatch(stringEntry.getKey(), managerConfigMap.get(propertySource.getName())) || ApolloExtendUtils.predicateMatch(stringEntry.getKey(), ApolloExtendUtils.skipMatchConfig()))
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
