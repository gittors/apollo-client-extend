package com.gittors.apollo.extend.chain;

import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.ConfigService;
import com.ctrip.framework.apollo.core.ConfigConsts;
import com.ctrip.framework.apollo.enums.ConfigSourceType;
import com.ctrip.framework.apollo.spring.config.ConfigPropertySource;
import com.ctrip.framework.apollo.spring.config.ConfigPropertySourceFactory;
import com.ctrip.framework.apollo.spring.util.SpringInjector;
import com.gittors.apollo.extend.chain.chain.AbstractLinkedProcessor;
import com.gittors.apollo.extend.common.constant.CommonApolloConstant;
import com.gittors.apollo.extend.common.spi.ServiceLookUp;
import com.gittors.apollo.extend.spi.ApolloExtendManageNamespacePostProcessor;
import com.gittors.apollo.extend.spi.ManageNamespaceConfigClass;
import com.gittors.apollo.extend.support.ApolloExtendPostProcessorDelegate;
import com.gittors.apollo.extend.utils.ApolloExtendUtils;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.source.ConfigurationPropertySources;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.StandardEnvironment;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author zlliu
 * @date 2020/8/29 15:41
 */
public abstract class ApolloExtendNameSpaceInjectorAdapter extends AbstractLinkedProcessor<ConfigurableEnvironment> {
    /**
     * 分割器，分割符：","
     */
    private static final Splitter NAMESPACE_SPLITTER =
            Splitter.on(CommonApolloConstant.DEFAULT_SEPARATOR)
            .omitEmptyStrings().trimResults();

    private final ConfigPropertySourceFactory configPropertySourceFactory = SpringInjector
            .getInstance(ConfigPropertySourceFactory.class);

    protected final Map<String, ManageNamespaceConfigClass> configClassMap = Maps.newLinkedHashMap();

    private final List<ApolloExtendManageNamespacePostProcessor> postProcessors =
            ServiceLookUp.loadAllOrdered(ApolloExtendManageNamespacePostProcessor.class);

    /**
     * 注册命名空间
     * @param environment
     * @param namespaceSet
     */
    protected void doInjector(ConfigurableEnvironment environment, Set<String> namespaceSet) {
        for (String namespace : namespaceSet) {
            parse(environment, namespace);
        }
        if (MapUtils.isNotEmpty(this.configClassMap)) {
            CompositePropertySource composite;
            for (ManageNamespaceConfigClass configClass : configClassMap.values()) {
                if (configClass.getConfigPropertySource() != null) {
                    composite = new CompositePropertySource(configClass.getCompositePropertySourceName());
                    composite.addPropertySource(configClass.getConfigPropertySource());
                    environment.getPropertySources().addLast(composite);
                }
            }
            // invoke post processor
            ApolloExtendPostProcessorDelegate.invokeManageNamespacePostProcessor(environment, postProcessors, Lists.newArrayList(configClassMap.values()));
        }
    }

    protected void parse(ConfigurableEnvironment environment, String namespace) {
        ManageNamespaceConfigClass namespaceConfigClass = new ManageNamespaceConfigClass(namespace, ConfigService.getConfig(namespace));
        namespaceConfigClass.setManageConfigPrefix(environment.getProperty(CommonApolloConstant.APOLLO_EXTEND_NAMESPACE_PREFIX, CommonApolloConstant.APOLLO_EXTEND_NAMESPACE));
        Object configClass = null;
        do {
            configClass = doParse(environment, namespaceConfigClass, configClass);
        }
        while (configClass != null);
        this.configClassMap.put(namespace, namespaceConfigClass);
    }

    protected Object doParse(ConfigurableEnvironment environment, ManageNamespaceConfigClass namespaceConfigClass, Object configClass) {
        Config config = namespaceConfigClass.getConfig();
        //  Load failed skip
        if (config.getSourceType() == null ||
                config.getSourceType() == ConfigSourceType.NONE) {
            return null;
        }
        String namespaceConfig = config.getProperty(namespaceConfigClass.getManageConfigPrefix(), null);
        if (StringUtils.isNotBlank(namespaceConfig)) {
            Set<String> namespaceSet = parseNamespace(namespaceConfig);
            //  过滤掉自己，避免死递归
            namespaceSet = namespaceSet.stream()
                    .filter(namespace -> !StringUtils.equalsIgnoreCase(namespace, namespaceConfigClass.getNamespace()))
                    .collect(Collectors.toSet());
            for (String namespace : namespaceSet) {
                parse(environment, namespace);
            }
        }

        //  合并当前 Spring环境，并添加配置项用于查找
        ConfigPropertySource configPropertySource = configPropertySourceFactory.getConfigPropertySource(namespaceConfigClass.getNamespace(), config);
        ConfigurableEnvironment standardEnvironment = new StandardEnvironment();
        standardEnvironment.merge(environment);
        standardEnvironment.getPropertySources().addLast(configPropertySource);

        ConfigurationPropertySources.attach(standardEnvironment);

        String configPrefix = standardEnvironment.getProperty(CommonApolloConstant.PROPERTY_SOURCE_CONFIG_SUFFIX, CommonApolloConstant.PROPERTY_SOURCE_CONFIG_DEFAULT_SUFFIX);

        namespaceConfigClass.setCompositePropertySourceName(ApolloExtendUtils.getPropertySourceName(standardEnvironment, configPrefix, namespaceConfigClass.getNamespace()));
        namespaceConfigClass.setConfigPropertySource(configPropertySource);

        return null;
    }

    /**
     * 解析配置，","号分割
     * @param namespaceKey
     * @return
     */
    protected Set<String> parseNamespace(String namespaceKey) {
        List<String> extensionNamespaceList = NAMESPACE_SPLITTER.splitToList(namespaceKey);
        extensionNamespaceList = extensionNamespaceList.stream()
                .filter(namespace -> !ConfigConsts.NAMESPACE_APPLICATION.equals(namespace))
                .collect(Collectors.toList());
        Set<String> namespaceSet = new HashSet<>();
        namespaceSet.addAll(extensionNamespaceList);
        return namespaceSet;
    }

}
