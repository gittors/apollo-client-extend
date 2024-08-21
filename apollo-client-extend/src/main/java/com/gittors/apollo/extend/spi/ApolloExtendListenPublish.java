package com.gittors.apollo.extend.spi;

import com.gittors.apollo.extend.common.constant.CommonApolloConstant;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * @author zlliu
 * @date 2022/08/21 13:14
 */
public interface ApolloExtendListenPublish<T> {
    /**
     * 发布更新事件
     * {@link CommonApolloConstant#APOLLO_EXTEND_LISTEN_KEY_SUFFIX}
     *
     * @param data
     */
    void doPublish(ConfigurableApplicationContext context, T data);
}
