package com.gittors.apollo.extend.chain;

import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.ConfigService;
import com.ctrip.framework.apollo.enums.ConfigSourceType;
import com.ctrip.framework.apollo.spring.config.PropertySourcesConstants;
import com.gittors.apollo.extend.chain.chain.AbstractLinkedProcessor;
import com.gittors.apollo.extend.common.constant.CommonApolloConstant;
import com.gittors.apollo.extend.common.env.SimplePropertySource;
import com.gittors.apollo.extend.common.service.ServiceLookUp;
import com.gittors.apollo.extend.env.SimpleCompositePropertySource;
import com.gittors.apollo.extend.spi.ApolloExtendNamespacePostProcessor;
import com.gittors.apollo.extend.support.ApolloExtendFactory;
import com.gittors.apollo.extend.support.ApolloExtendPostProcessorDelegate;
import com.gittors.apollo.extend.utils.ApolloExtendUtils;
import com.google.common.collect.Maps;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author zlliu
 * @date 2020/8/29 15:41
 */
public abstract class ApolloExtendNameSpaceInjectorAdapter extends AbstractLinkedProcessor<ConfigurableApplicationContext> {

    protected final Map<String, ManageNamespaceConfigClass> configClassMap = Maps.newLinkedHashMap();

    private final ApolloExtendNamespacePostProcessor postProcessor =
            ServiceLookUp.loadPrimary(ApolloExtendNamespacePostProcessor.class);

    /**
     * 注册命名空间
     * @param context
     * @param namespaceSet
     */
    protected void doInjector(ConfigurableApplicationContext context, Set<String> namespaceSet) {
        for (String namespace : namespaceSet) {
            parse(context.getEnvironment(), namespace);
        }
        if (MapUtils.isNotEmpty(this.configClassMap)) {
            SimpleCompositePropertySource bootstrapComposite = new SimpleCompositePropertySource(CommonApolloConstant.APOLLO_BOOTSTRAP_PROPERTY_SOURCE_NAME);
            for (ManageNamespaceConfigClass configClass : configClassMap.values()) {
                bootstrapComposite.addPropertySource(configClass.getSimplePropertySource());
            }
            if (context.getEnvironment().getPropertySources().contains(PropertySourcesConstants.APOLLO_BOOTSTRAP_PROPERTY_SOURCE_NAME)) {
                context.getEnvironment().getPropertySources()
                        .addAfter(PropertySourcesConstants.APOLLO_BOOTSTRAP_PROPERTY_SOURCE_NAME, bootstrapComposite);
            } else {
                context.getEnvironment().getPropertySources()
                        .addAfter(PropertySourcesConstants.APOLLO_PROPERTY_SOURCE_NAME, bootstrapComposite);
            }
            // invoke post processor
            ApolloExtendPostProcessorDelegate.invokeManagerPostProcessor(postProcessor, context.getEnvironment());
        }
    }

    /**
     * 解析扩展命名空间配置   {@link CommonApolloConstant#APOLLO_EXTEND_NAMESPACE}
     *
     * @param environment
     * @param namespace     命名空间管理配置名称
     */
    protected void parse(ConfigurableEnvironment environment, String namespace) {
        ManageNamespaceConfigClass configClass = new ManageNamespaceConfigClass(namespace, ConfigService.getConfig(namespace));
        configClass.setManageConfigPrefix(environment.getProperty(CommonApolloConstant.APOLLO_EXTEND_NAMESPACE_PREFIX, CommonApolloConstant.APOLLO_EXTEND_NAMESPACE));
        Object object = null;
        do {
            object = doParse(environment, configClass, object);
        }
        while (object != null);
        this.configClassMap.put(namespace, configClass);
    }

    /**
     * 根据命名空间管理配置，继续递归解析
     * @param environment
     * @param configClass
     * @param object
     * @return
     */
    protected Object doParse(ConfigurableEnvironment environment, ManageNamespaceConfigClass configClass, Object object) {
        Config config = configClass.getConfig();
        //  Load failed skip
        if (config.getSourceType() == null ||
                config.getSourceType() == ConfigSourceType.NONE) {
            return null;
        }
        String namespaceConfig = config.getProperty(configClass.getManageConfigPrefix(), null);
        if (StringUtils.isNotBlank(namespaceConfig)) {
            Set<String> namespaceSet = ApolloExtendUtils.parseNamespace(namespaceConfig);
            //  过滤掉自己，避免死递归
            namespaceSet = namespaceSet.stream()
                    .filter(namespace -> !StringUtils.equalsIgnoreCase(namespace, configClass.getNamespace()))
                    .collect(Collectors.toSet());
            //  根据管理配置的命名空间名称，继续递归解析是否有管理配置
            for (String namespace : namespaceSet) {
                parse(environment, namespace);
            }
        }
        String propertySourceName = ApolloExtendUtils.getPropertySourceName(configClass.getNamespace());
        ApolloExtendFactory.PropertySourceFactory factory =
                ApolloExtendUtils.getPropertySourceFactory(propertySourceName, configClass.getNamespace(), config);
        //  创建 PropertySource 并缓存
        SimplePropertySource simplePropertySource = factory.createPropertySource(true, true, null);
        configClass.setSimplePropertySource(simplePropertySource);
        return null;
    }

}
