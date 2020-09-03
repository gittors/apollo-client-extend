package com.gittors.apollo.extend.support;

import com.gittors.apollo.extend.spi.ApolloExtendManageNamespacePostProcessor;
import com.gittors.apollo.extend.spi.ManageNamespaceConfigClass;
import org.springframework.core.env.ConfigurableEnvironment;

import java.util.List;

/**
 * @author zlliu
 * @date 2020/9/3 11:02
 */
public final class ApolloExtendPostProcessorDelegate {
    private ApolloExtendPostProcessorDelegate() {
    }

    public static void invokeManageNamespacePostProcessor(ConfigurableEnvironment environment,
                                                          List<ApolloExtendManageNamespacePostProcessor> postProcessors,
                                                          List<ManageNamespaceConfigClass> configClasses) {
        for (ApolloExtendManageNamespacePostProcessor processor : postProcessors) {
            processor.postProcessManageNamespace(environment, configClasses);
        }
    }
}
