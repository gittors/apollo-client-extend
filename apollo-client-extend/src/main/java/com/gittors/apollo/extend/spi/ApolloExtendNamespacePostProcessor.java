package com.gittors.apollo.extend.spi;

import com.gittors.apollo.extend.common.service.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * 项目初始化，命名空间后置处理
 *
 * @author zlliu
 * @date 2020/9/3 10:52
 */
public interface ApolloExtendNamespacePostProcessor extends Ordered {

    /**
     * 管理命名空间后置处理器
     *
     * @param environment
     */
    void postProcess(ConfigurableEnvironment environment);
}
