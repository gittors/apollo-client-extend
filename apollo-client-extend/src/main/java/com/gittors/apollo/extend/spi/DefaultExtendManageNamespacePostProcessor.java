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
 * 这个缺省的实现：
 * 1、假如配置了：listen.key.addMap.application2 = my.key
 * 2、即根据1的配置，将命名空间 application2的配置部分生效，生效的KEY为"my.key"
 * 注：如果不需要这个功能，通过SPI替换一个空的实现即可
 *
 * @author zlliu
 * @date 2020/9/3 11:19
 */
public class DefaultExtendManageNamespacePostProcessor implements ApolloExtendManageNamespacePostProcessor {

    @Override
    public void postProcessManageNamespace(ConfigurableEnvironment environment, List<ManageNamespaceConfigClass> configClasses) {
        Set<String> namespaceSet = configClasses.stream()
                .map(ManageNamespaceConfigClass::getNamespace).collect(Collectors.toSet());
        //  获得管理配置，部分配置生效等
        Map<String, Map.Entry<Boolean, Set<String>>> managerConfigMap =
                ApolloExtendUtils.getManagerConfig(environment, namespaceSet, ChangeType.ADD);

        List<ConfigPropertySource> addPropertySourceList = configClasses.stream()
                        .map(configClass -> (ConfigPropertySource) configClass.getConfigPropertySource())
                        .collect(Collectors.toList());

        //  过滤Apollo配置，使其部分生效
        addPropertySourceList.forEach(propertySource ->
                ApolloExtendUtils.managerConfigHandler(propertySource, managerConfigMap.get(propertySource.getName()))
        );

    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
