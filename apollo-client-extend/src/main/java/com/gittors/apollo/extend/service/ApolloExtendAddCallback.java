package com.gittors.apollo.extend.service;

import com.ctrip.framework.apollo.enums.ConfigSourceType;
import com.ctrip.framework.apollo.model.ConfigChangeEvent;
import com.ctrip.framework.apollo.spring.config.ConfigPropertySource;
import com.ctrip.framework.apollo.spring.config.ConfigPropertySourceFactory;
import com.ctrip.framework.apollo.spring.config.PropertySourcesConstants;
import com.ctrip.framework.apollo.spring.util.SpringInjector;
import com.gittors.apollo.extend.callback.AbstractApolloExtendCallback;
import com.gittors.apollo.extend.common.constant.CommonApolloConstant;
import com.gittors.apollo.extend.common.enums.ChangeType;
import com.gittors.apollo.extend.common.utils.ListUtils;
import com.gittors.apollo.extend.support.ext.DefaultConfigExt;
import com.gittors.apollo.extend.utils.ApolloExtendUtils;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;

/**
 * 监听： {@link CommonApolloConstant#APOLLO_EXTEND_ADD_CALLBACK_CONFIG}
 *
 * @author zlliu
 * @date 2021/5/8 13:19
 */
@Slf4j
public class ApolloExtendAddCallback extends AbstractApolloExtendDynamicCallback implements EnvironmentAware {
    public static final String BEAN_NAME = "apolloExtendAddCallback";

    private final ConfigPropertySourceFactory configPropertySourceFactory = SpringInjector
            .getInstance(ConfigPropertySourceFactory.class);

    private ConfigurableEnvironment environment;

    public ApolloExtendAddCallback(ConfigurableEnvironment environment) {
        super(environment);
    }

    @Override
    protected String getConfigPrefix() {
        return CommonApolloConstant.APOLLO_EXTEND_ADD_CALLBACK_CONFIG;
    }

//    @Override
//    public void callback(String oldValue, String newValue, Object... objects) {
//        //  当前更新的key, 如： listen.key.addMap.application-test
//        String key = (String) objects[0];
//        ConfigChangeEvent changeEvent = (ConfigChangeEvent) objects[1];
//        //  当前更新的命名空间
//        String namespace = changeEvent.getNamespace();
//        String managerNamespace = key.substring(key.lastIndexOf(".") + 1);
//
//        //  TODO 1、根据当前命名空间key，找到对应管理配置：apollo.extend.namespace 的值
//        String propertySourceName = null;
//        //  application 命名空间
//        if (CommonApolloConstant.NAMESPACE_APPLICATION.equals(namespace)) {
//            propertySourceName = PropertySourcesConstants.APOLLO_BOOTSTRAP_PROPERTY_SOURCE_NAME;
//        } else {
//            propertySourceName = ApolloExtendUtils.getPropertySourceName(environment, managerNamespace);
//        }
//        MutablePropertySources mutablePropertySources = environment.getPropertySources();
//        PropertySource propertySource = mutablePropertySources.get(propertySourceName);
//        if (propertySource == null) {
//            log.warn("#callback propertySource is null");
//            return;
//        }
//        //  获得 CommonApolloConstant.APOLLO_EXTEND_NAMESPACE 的配置值
//        String managerNamespaceConfig = (String) propertySource.getProperty(CommonApolloConstant.APOLLO_EXTEND_NAMESPACE);
//        List<String> namespaceList = NAMESPACE_SPLITTER.splitToList(managerNamespaceConfig);
//        if (StringUtils.isBlank(managerNamespaceConfig) || CollectionUtils.isEmpty(namespaceList) ||
//                !namespaceList.contains(managerNamespace)) {
//            log.warn("#callback managerNamespaceConfig illegal");
//            return;
//        }
//        //  TODO 2、如果1的命名空间已存在，且修改了：listen.key.addMap 配置的值，直接根据配置刷新Spring环境
//        Set<String> newNamespaceSet = Sets.newHashSet(managerNamespace);
//        Map<String, Map.Entry<Boolean, Set<String>>> managerConfigMap = ApolloExtendUtils.getManagerConfig(environment, newNamespaceSet, ChangeType.ADD);
//
//        Optional<ConfigPropertySource> optional = configPropertySourceFactory.getAllConfigPropertySources()
//                .stream()
//                .filter(ListUtils.distinctByKey(PropertySource::getName))
//                .filter(configPropertySource -> configPropertySource.getSource().getSourceType() != ConfigSourceType.NONE)
//                .filter(configPropertySource -> managerNamespace.equals(configPropertySource.getName()))
//                .findFirst();
//        if (!optional.isPresent()) {
//            log.warn("#callback configPropertySource can't find");
//            return;
//        }
//        ConfigPropertySource configPropertySource = optional.get();
//        DefaultConfigExt defaultConfig = (DefaultConfigExt) configPropertySource.getSource();
//        Properties properties = new Properties();
//        //  1/筛选生效属性
//        Properties sourceProperties = defaultConfig.getConfigRepository().getConfig();
//        sourceProperties.stringPropertyNames()
//                .stream()
//                .filter(configKey -> ApolloExtendUtils.predicateMatch(configKey, managerConfigMap.get(configPropertySource.getName())) || ApolloExtendUtils.predicateMatch(configKey, ApolloExtendUtils.skipMatchConfig()))
//                .forEach(configKey -> properties.setProperty(configKey, sourceProperties.getProperty(configKey, "")));
//
//        //  2/刷新对象
//        defaultConfig.updateConfig(properties, configPropertySource.getSource().getSourceType());
//    }

    @Override
    protected ChangeType changeType() {
        return ChangeType.ADD;
    }

    @Override
    protected void propertiesHandler(Properties properties, DefaultConfigExt defaultConfig, Map.Entry<Boolean, Set<String>> configEntry) {
        //  1/筛选生效属性
        Properties sourceProperties = defaultConfig.getConfigRepository().getConfig();
        sourceProperties.stringPropertyNames()
                .stream()
                .filter(configKey -> ApolloExtendUtils.predicateMatch(configKey, configEntry) || ApolloExtendUtils.predicateMatch(configKey, ApolloExtendUtils.skipMatchConfig()))
                .forEach(configKey -> properties.setProperty(configKey, sourceProperties.getProperty(configKey, "")));
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = (ConfigurableEnvironment) environment;
    }
}
