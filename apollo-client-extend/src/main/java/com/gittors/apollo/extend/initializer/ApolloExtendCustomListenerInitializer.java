package com.gittors.apollo.extend.initializer;

import com.ctrip.framework.apollo.core.ConfigConsts;
import com.ctrip.framework.apollo.spring.config.ConfigPropertySource;
import com.gittors.apollo.extend.callback.ApolloExtendCallback;
import com.gittors.apollo.extend.common.constant.CommonApolloConstant;
import com.gittors.apollo.extend.context.ApolloExtendContext;
import com.gittors.apollo.extend.utils.ApolloExtendUtils;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertySource;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author zlliu
 * @date 2020/8/18 17:16
 */
public class ApolloExtendCustomListenerInitializer implements ApplicationRunner {

    private static final Splitter NAMESPACE_SPLITTER =
            Splitter.on(CommonApolloConstant.DEFAULT_SEPARATOR)
                    .omitEmptyStrings().trimResults();

    @Autowired(required = false)
    private List<ApolloExtendCallback> apolloExtendCallbacks = Collections.emptyList();

    @Autowired
    private ConfigurableEnvironment environment;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        Map<String, ApolloExtendCallback> callbackMap = new HashMap<>(8);
        for (ApolloExtendCallback callback : apolloExtendCallbacks) {
            for (String key : callback.keyList()) {
                callbackMap.put(key, callback);
            }
        }
        //  初始化回调
        ApolloExtendContext.INSTANCE.initCallbackMap(callbackMap);

        Set<String> namespaceSet = new HashSet<>();
        namespaceSet.addAll(getAllNamespace(environment));
        namespaceSet.add(ConfigConsts.NAMESPACE_APPLICATION);

        for (String namespace : namespaceSet) {
            ApolloExtendUtils.addListener(namespace, callbackMap);
        }
    }

    /**
     * 从当前 Spring环境获得所有管理的命名空间
     * @param environment
     * @return
     */
    private Set<String> getAllNamespace(ConfigurableEnvironment environment) {
        Set<String> namespaceSet = new HashSet<>();
        for (PropertySource<?> propertySource : environment.getPropertySources()) {
            String manageNamespaces = ConfigConsts.NAMESPACE_APPLICATION;
            if (propertySource instanceof CompositePropertySource) {
                List<String> namespaceList = ((CompositePropertySource) propertySource)
                        .getPropertySources()
                        .stream()
                        .filter(p -> p instanceof ConfigPropertySource)
                        .map(PropertySource::getName)
                        .collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(namespaceList)) {
                    manageNamespaces = Joiner.on(CommonApolloConstant.DEFAULT_SEPARATOR)
                            .join(namespaceList.toArray(new String[]{}));
                }
            }
            List<String> manageNamespacesList = NAMESPACE_SPLITTER.splitToList(manageNamespaces);

            namespaceSet.addAll(manageNamespacesList);
        }
        return namespaceSet;
    }

}
