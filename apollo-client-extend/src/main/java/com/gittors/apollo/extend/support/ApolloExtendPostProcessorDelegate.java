package com.gittors.apollo.extend.support;

import com.gittors.apollo.extend.spi.ApolloExtendNamespacePostProcessor;
import com.gittors.apollo.extend.chain.ManagerConfigClass;
import org.apache.commons.collections4.CollectionUtils;
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
    public static void invokePostProcessors(List<ApolloExtendNamespacePostProcessor> postProcessors,
                                            ConfigurableEnvironment environment,
                                            List<ManagerConfigClass> configClasses) {
        if (CollectionUtils.isNotEmpty(postProcessors)) {
            for (ApolloExtendNamespacePostProcessor processor : postProcessors) {
                processor.postProcess(environment);
            }
        }
    }

    public static void invokePostProcessor(ApolloExtendNamespacePostProcessor postProcessor,
                                           ConfigurableEnvironment environment) {
        if (postProcessor != null) {
            postProcessor.postProcess(environment);
        }
    }
}
