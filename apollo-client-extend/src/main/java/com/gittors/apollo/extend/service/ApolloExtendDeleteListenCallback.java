package com.gittors.apollo.extend.service;

import com.gittors.apollo.extend.common.constant.CommonApolloConstant;
import com.gittors.apollo.extend.common.enums.ChangeType;
import com.gittors.apollo.extend.support.ApolloExtendStringMapEntry;
import com.gittors.apollo.extend.support.ext.DefaultConfigExt;
import com.gittors.apollo.extend.utils.ApolloExtendUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.env.ConfigurableEnvironment;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * 监听： {@link CommonApolloConstant#APOLLO_EXTEND_DELETE_CALLBACK_CONFIG}
 *  配置处理
 * @author zlliu
 * @date 2021/5/8 13:19
 */
@Slf4j
public class ApolloExtendDeleteListenCallback extends AbstractApolloExtendListenCallback {
    public static final String BEAN_NAME = "apolloExtendDeleteListenCallback";

    public ApolloExtendDeleteListenCallback(ConfigurableEnvironment environment) {
        super(environment);
    }

    @Override
    protected String getConfigPrefix() {
        return CommonApolloConstant.APOLLO_EXTEND_DELETE_CALLBACK_CONFIG;
    }

    @Override
    protected boolean check(String managerNamespaceConfig, String managerNamespace) {
        if (StringUtils.isBlank(managerNamespaceConfig)) {
            return false;
        }
        List<String> namespaceList = NAMESPACE_SPLITTER.splitToList(managerNamespaceConfig);
        if (CollectionUtils.isNotEmpty(namespaceList) && namespaceList.contains(managerNamespace)) {
            log.warn("#callback managerNamespaceConfig illegal");
            return true;
        }
        return false;
    }

    @Override
    protected ChangeType judgmentChangeType(String managerNamespaceConfig, String managerNamespace) {
        return ChangeType.DELETE;
    }

    @Override
    protected void propertiesBeforeHandler(final Properties properties, final DefaultConfigExt defaultConfig, Map.Entry<Boolean, Set<String>> configEntry,
                                           final ChangeType changeType) {
        //  剔除掉应该失效的属性
        Properties sourceProperties = defaultConfig.getConfigRepository().getConfig();
        sourceProperties.stringPropertyNames()
                .stream()
                .filter(configKey -> !ApolloExtendUtils.predicateMatch(configKey, configEntry) || ApolloExtendUtils.predicateMatch(configKey, ApolloExtendUtils.skipMatchConfig()))
                .forEach(configKey -> properties.setProperty(configKey, sourceProperties.getProperty(configKey, "")));
    }

    @Override
    protected void propertiesAfterHandler(DefaultConfigExt defaultConfig, Map.Entry<Boolean, Set<String>> configEntry, final ChangeType changeType) {
        //  设置属性部分失效
        defaultConfig.addPropertiesCallBack(updateProperties -> {
            Properties filterProperties = new Properties();
            Properties property = (Properties) updateProperties;
            property.entrySet().stream()
                    .map(objectEntry -> new ApolloExtendStringMapEntry(String.valueOf(objectEntry.getKey()), String.valueOf(objectEntry.getValue())))
                    .filter(stringEntry -> !ApolloExtendUtils.predicateMatch(stringEntry.getKey(), configEntry) || ApolloExtendUtils.predicateMatch(stringEntry.getKey(), ApolloExtendUtils.skipMatchConfig()))
                    .forEach(stringEntry -> filterProperties.setProperty(stringEntry.getKey(), stringEntry.getValue()));
            return filterProperties;
        });
    }
}
