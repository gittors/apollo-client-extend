package com.gittors.apollo.extend.spi;

import com.gittors.apollo.extend.common.service.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;

import java.util.List;

/**
 * @author zlliu
 * @date 2020/9/3 10:52
 */
public interface ApolloExtendManageNamespacePostProcessor extends Ordered {

    /**
     * 管理命名空间后置处理器
     *
     * @param environment
     * @param configClasses     代注册的命名空间配置
     */
    void postProcessNamespaceManager(ConfigurableEnvironment environment, List<ManageNamespaceConfigClass> configClasses);
}
