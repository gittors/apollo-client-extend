package com.gittors.apollo.extend.spi;

import com.ctrip.framework.apollo.spring.config.ConfigPropertySource;
import com.gittors.apollo.extend.common.enums.ChangeType;
import com.gittors.apollo.extend.common.spi.Ordered;
import com.gittors.apollo.extend.utils.ApolloExtendUtils;
import org.springframework.core.env.ConfigurableEnvironment;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author zlliu
 * @date 2020/9/3 11:19
 */
public class DefaultExtendManageNamespacePostProcessor implements ApolloExtendManageNamespacePostProcessor {

    @Override
    public void postProcessManageNamespace(ConfigurableEnvironment environment, List<ManageNamespaceConfigClass> configClasses) {
        Set<String> namespaceSet =
                configClasses.stream()
                        .map(ManageNamespaceConfigClass::getNamespace)
                        .collect(Collectors.toSet());
        Map<String, Map.Entry<Boolean, Set<String>>> managerConfigMap =
                ApolloExtendUtils.getManagerConfig(environment, namespaceSet, ChangeType.ADD);

        List<ConfigPropertySource> addPropertySourceList = configClasses.stream()
                        .map(configClass -> (ConfigPropertySource) configClass.getConfigPropertySource())
                        .collect(Collectors.toList());

        addPropertySourceList.forEach(propertySource -> ApolloExtendUtils.managerConfigHandler(propertySource, managerConfigMap.get(propertySource.getName())));

    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
