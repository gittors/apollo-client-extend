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

    /**
     * 命名空间后置处理
     *
     * @param environment
     * @param postProcessors
     * @param configClasses
     */
    public static void invokeManagerPostProcessor(ConfigurableEnvironment environment,
                                                  List<ApolloExtendManageNamespacePostProcessor> postProcessors,
                                                  List<ManageNamespaceConfigClass> configClasses) {
        for (ApolloExtendManageNamespacePostProcessor processor : postProcessors) {
            processor.postProcessNamespaceManager(environment, configClasses);
        }
    }
}
