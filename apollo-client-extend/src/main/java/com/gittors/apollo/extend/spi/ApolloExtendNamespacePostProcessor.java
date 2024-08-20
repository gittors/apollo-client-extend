package com.gittors.apollo.extend.spi;

import com.gittors.apollo.extend.common.service.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * @author zlliu
 * @date 2020/9/3 10:52
 */
public interface ApolloExtendNamespacePostProcessor extends Ordered {

    /**
     * 管理命名空间后置处理器
     *
     * @param environment
     */
    void postProcessNamespaceManager(ConfigurableEnvironment environment);
}
