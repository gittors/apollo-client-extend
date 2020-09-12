package com.gittors.apollo.extend.initializer;

import com.ctrip.framework.apollo.enums.ConfigSourceType;
import com.ctrip.framework.apollo.spring.config.ConfigPropertySourceFactory;
import com.ctrip.framework.apollo.spring.util.SpringInjector;
import com.gittors.apollo.extend.callback.ApolloExtendCallback;
import com.gittors.apollo.extend.common.utils.ListUtils;
import com.gittors.apollo.extend.context.ApolloExtendContext;
import com.gittors.apollo.extend.utils.ApolloExtendUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.PropertySource;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author zlliu
 * @date 2020/8/18 17:16
 */
public class ApolloExtendCustomListenerInitializer implements ApplicationRunner {

    private final ConfigPropertySourceFactory configPropertySourceFactory = SpringInjector
            .getInstance(ConfigPropertySourceFactory.class);

    @Autowired(required = false)
    private List<ApolloExtendCallback> apolloExtendCallbacks = Collections.emptyList();

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

        List<String> namespaceList =
                configPropertySourceFactory.getAllConfigPropertySources()
                        .stream()
                        .filter(ListUtils.distinctByKey(PropertySource::getName))
                        .filter(configPropertySource -> configPropertySource.getSource().getSourceType() != ConfigSourceType.NONE)
                        .map(PropertySource::getName)
                        .collect(Collectors.toList());
        for (String namespace : namespaceList) {
            ApolloExtendUtils.addListener(namespace, callbackMap);
        }
    }

}
