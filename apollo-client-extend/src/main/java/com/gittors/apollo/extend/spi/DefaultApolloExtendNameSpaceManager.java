package com.gittors.apollo.extend.spi;

import com.ctrip.framework.apollo.ConfigChangeListener;
import com.ctrip.framework.apollo.ConfigService;
import com.ctrip.framework.apollo.core.ConfigConsts;
import com.ctrip.framework.apollo.enums.ConfigSourceType;
import com.gittors.apollo.extend.common.constant.CommonApolloConstant;
import com.gittors.apollo.extend.common.constant.CommonConstant;
import com.gittors.apollo.extend.common.context.ApolloPropertySourceContext;
import com.gittors.apollo.extend.common.enums.ChangeType;
import com.gittors.apollo.extend.common.env.SimplePropertySource;
import com.gittors.apollo.extend.common.service.Ordered;
import com.gittors.apollo.extend.env.SimpleCompositePropertySource;
import com.gittors.apollo.extend.support.ApolloExtendFactory;
import com.gittors.apollo.extend.support.ext.ApolloClientExtendConfig;
import com.gittors.apollo.extend.utils.ApolloExtendStringUtils;
import com.gittors.apollo.extend.utils.ApolloExtendUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 这个缺省的实现：
 * Apollo的管理配置：{@link CommonApolloConstant#APOLLO_EXTEND_NAMESPACE}
 * 的新增和删除的处理类
 *
 * @author zlliu
 * @date 2020/8/26 10:38
 */
@Slf4j
public class DefaultApolloExtendNameSpaceManager implements ApolloExtendNameSpaceManager {

    private ConfigurableEnvironment environment;
    private ConfigurableListableBeanFactory beanFactory;

    @Override
    public void setApplicationContext(ConfigurableApplicationContext applicationContext) {
        if (this.environment == null) {
            this.environment = applicationContext.getEnvironment();
        }
        if (this.beanFactory == null) {
            this.beanFactory = applicationContext.getBeanFactory();
        }
    }

    @Override
    public Map<String, Map<String, String>> getAddNamespaceConfig(Set<String> needAddNamespaceSet) {
        if (CollectionUtils.isEmpty(needAddNamespaceSet)) {
            return Maps.newHashMap();
        }
        //  1.获得新增命名空间的propertySource
        List<SimplePropertySource> addPropertySourceList = doGetAddNamespace(needAddNamespaceSet);
        if (CollectionUtils.isEmpty(addPropertySourceList)) {
            return Maps.newHashMap();
        }
        ConfigurableEnvironment standardEnvironment = ApolloExtendUtils.mergeEnvironment(environment, addPropertySourceList);

        //  2.获得合并的配置项
        Map<String, Map.Entry<Boolean, Set<String>>> managerConfigMap = ApolloExtendUtils.getManagerConfig(standardEnvironment, needAddNamespaceSet, ChangeType.ADD);

        //  3.过滤掉"apollo extend管理配置"之外的配置项【用户真正需要关心的配置项】
        Map<String, Map<String, String>> addConfig = filter(addPropertySourceList, managerConfigMap);

        //  4.新增配置项，刷新spring环境
        refreshAddEnvironment(addPropertySourceList, managerConfigMap);

        return addConfig;
    }

    @Override
    public Map<String, Map<String, String>> getDeleteNamespaceConfig(Set<String> needDeleteNamespaceSet) {
        if (CollectionUtils.isEmpty(needDeleteNamespaceSet)) {
            return Maps.newHashMap();
        }
        //  1.获得需删除的命名空间的propertySource
        List<SimplePropertySource> deletePropertySourceList = doGetDeleteNamespace(needDeleteNamespaceSet);
        if (CollectionUtils.isEmpty(deletePropertySourceList)) {
            return Maps.newHashMap();
        }
        //  2.获得合并的配置项
        Map<String, Map.Entry<Boolean, Set<String>>> managerConfigMap = ApolloExtendUtils.getManagerConfig(environment, needDeleteNamespaceSet, ChangeType.DELETE);

        //  3.过滤掉"apollo extend管理配置"之外的配置项【用户真正需要关心的配置项】
        //  筛选出需删除的配置项，用于返回
        Map<String, Map<String, String>> deleteConfig = filter(deletePropertySourceList, managerConfigMap);

        //  4.删除配置项，刷新spring环境
        refreshDelEnvironment(deletePropertySourceList, managerConfigMap);

        return deleteConfig;
    }

    /**
     * 过滤掉管理配置
     * @param propertySourceList
     * @param managerConfigMap  存放的是应该删除的KEY
     * @return
     */
    protected Map<String, Map<String, String>> filter(List<SimplePropertySource> propertySourceList, Map<String, Map.Entry<Boolean, Set<String>>> managerConfigMap) {
        Map<String, Map<String, String>> propertiesMap = Maps.newHashMap();
        if (CollectionUtils.isEmpty(propertySourceList) || MapUtils.isEmpty(managerConfigMap)) {
            return propertiesMap;
        }
        ApolloExtendFactory.DataFilter dataFilter = ApolloExtendUtils.getDataFilterPredicate(environment);
        for (SimplePropertySource propertySource : propertySourceList) {
            Map<String, String> map = dataFilter.filter(propertySource, managerConfigMap.get(propertySource.getNamespace()));
            propertiesMap.put(propertySource.getNamespace(), map);
        }
        return propertiesMap;
    }

    /**
     * 返回需新增的配置
     * @param needAddNamespaceSet
     */
    protected List<SimplePropertySource> doGetAddNamespace(Set<String> needAddNamespaceSet) {
        List<SimplePropertySource> addPropertySourceList = Lists.newArrayList();
        SimpleCompositePropertySource bootstrapComposite = ApolloExtendUtils.getCompositePropertySource(environment);
        for (String namespace : needAddNamespaceSet) {
            String propertySourceName = ApolloExtendUtils.getPropertySourceName(namespace);

            //  不存在有两种情况：
            //  1、新增的：直接新增
            //  2、删除有添加的：刷新对象，重新添加
            if (!bootstrapComposite.contains(propertySourceName)) {
                SimplePropertySource simplePropertySource = null;
                String backUpPropertySourceName = ApolloExtendStringUtils.format(propertySourceName, null, CommonConstant.PROPERTY_SOURCE_NAME_SUFFIX);
                //  有备份，说明之前删除过
                if (bootstrapComposite.contains(backUpPropertySourceName)) {
                    simplePropertySource = (SimplePropertySource) bootstrapComposite.get(backUpPropertySourceName);
                    //  删除备份的 PropertySource
                    bootstrapComposite.remove(backUpPropertySourceName);
                    if (simplePropertySource == null) {
                        ApolloExtendFactory.PropertySourceFactory factory =
                                ApolloExtendUtils.getPropertySourceFactory(propertySourceName, namespace, ConfigService.getConfig(namespace));
                        simplePropertySource = factory.createPropertySource(true, true, null);
                    } else {
                        ApolloClientExtendConfig defaultConfig = ((ApolloClientExtendConfig) simplePropertySource.getSource());
                        //  删除 listener
                        for (ConfigChangeListener changeListener : defaultConfig.getChangeListener()) {
                            if (changeListener != null) {
                                defaultConfig.removeChangeListener(changeListener);
                            }
                        }
                        defaultConfig.initialize();
                    }
                } else {
                    ApolloExtendFactory.PropertySourceFactory factory =
                            ApolloExtendUtils.getPropertySourceFactory(propertySourceName, namespace, ConfigService.getConfig(namespace));
                    simplePropertySource = factory.createPropertySource(true, true, null);
                }
                //  Load failed skip
                if (simplePropertySource != null && (simplePropertySource.getSource() == null ||
                        simplePropertySource.getSource().getSourceType() == ConfigSourceType.NONE)) {
                    continue;
                }
                addPropertySourceList.add(simplePropertySource);
            }
        }
        return addPropertySourceList;
    }

    protected void refreshAddEnvironment(List<SimplePropertySource> addPropertySourceList, Map<String, Map.Entry<Boolean, Set<String>>> managerConfigMap) {
        if (CollectionUtils.isEmpty(addPropertySourceList) || MapUtils.isEmpty(managerConfigMap)) {
            log.warn("#refreshAddEnvironmentPostHandler addPropertySourceList OR managerConfigMap is empty!");
            return;
        }
        SimpleCompositePropertySource bootstrapComposite = ApolloExtendUtils.getCompositePropertySource(environment);
        for (SimplePropertySource propertySource : addPropertySourceList) {
            ApolloExtendFactory.PropertyFilterPredicate filterPredicate = ApolloExtendUtils.getFilterPredicate(true);
            //  设置配置部分生效
            ApolloExtendUtils.configValidHandler(propertySource, managerConfigMap.get(propertySource.getNamespace()), filterPredicate);

            bootstrapComposite.addPropertySource(propertySource);
            //  添加监听器
            ApolloExtendUtils.addListener(propertySource, environment, beanFactory);
        }
    }

    /**
     * 根据配置的 "监听属性" 匹配需要移除的 命名空间
     *
     * @param needDeleteNamespaceSet
     * @return
     */
    protected List<SimplePropertySource> doGetDeleteNamespace(Set<String> needDeleteNamespaceSet) {
        if (CollectionUtils.isEmpty(needDeleteNamespaceSet)) {
            return Lists.newArrayList();
        }
        //  1.排除application
        //  2.根据 "需要删除的命名空间" namespaceSet 筛选
        //  3.判断是否包涵 "指定前缀" 的配置
        List<SimplePropertySource> list = ApolloPropertySourceContext.INSTANCE.getPropertySources()
                .stream()
                .filter(propertySource -> propertySource.getSource().getSourceType() != ConfigSourceType.NONE)
                .filter(propertySource -> !ConfigConsts.NAMESPACE_APPLICATION.equalsIgnoreCase(propertySource.getNamespace()))
                .filter(propertySource -> needDeleteNamespaceSet.contains(propertySource.getNamespace()))
                .collect(Collectors.toList())
                ;
        if (CollectionUtils.isNotEmpty(list)) {
            for (SimplePropertySource propertySource : list) {
                ApolloClientExtendConfig source = (ApolloClientExtendConfig) propertySource.getSource();
                source.initialize();
            }
            return list;
        }
        return Lists.newArrayList();
    }

    /**
     * 删除配置，刷新环境
     *
     * 注意：要删除的配置是在已生效的配置基础上删除的
     *
     * @param configPropertySourceList  需删除的配置项
     */
    protected void refreshDelEnvironment(List<SimplePropertySource> configPropertySourceList, Map<String, Map.Entry<Boolean, Set<String>>> managerConfigMap) {
        if (CollectionUtils.isEmpty(configPropertySourceList) || MapUtils.isEmpty(managerConfigMap)) {
            log.warn("#refreshDelEnvironmentPostHandler addPropertySourceList OR managerConfigMap is empty!");
            return;
        }
        if (!environment.getPropertySources().contains(CommonApolloConstant.APOLLO_BOOTSTRAP_PROPERTY_SOURCE_NAME)) {
            return;
        }
        //  1、删除 Spring环境配置
        //  2、根据监听Key，删除配置
        //  3、刷新对象、移除监听器、设置回调
        //  4、刷新 Spring环境配置
        SimpleCompositePropertySource bootstrapComposite = ApolloExtendUtils.getCompositePropertySource(environment);
        for (SimplePropertySource propertySource : configPropertySourceList) {
            //  获得命名空间对应的spring propertySource名称
            String propertySourceBackupName = ApolloExtendStringUtils.format(propertySource.getName(), null, CommonConstant.PROPERTY_SOURCE_NAME_SUFFIX);

            //  删除 propertySource 和 备份 propertySource
            bootstrapComposite.remove(propertySource.getName());
            bootstrapComposite.remove(propertySourceBackupName);

            ApolloExtendFactory.PropertyFilterPredicate filterPredicate = ApolloExtendUtils.getFilterPredicate(false);
            //  设置回调+更新对象
            ApolloExtendUtils.configValidHandler(propertySource, managerConfigMap.get(propertySource.getNamespace()), filterPredicate);

            //  刷新环境配置
            SimplePropertySource simplePropertySource = SimplePropertySource.of(propertySourceBackupName, propertySource);
            bootstrapComposite.addPropertySource(simplePropertySource);
        }
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
