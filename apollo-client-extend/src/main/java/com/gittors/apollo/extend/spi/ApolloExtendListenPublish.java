package com.gittors.apollo.extend.spi;

import com.gittors.apollo.extend.common.service.Ordered;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * @author zlliu
 * @date 2022/08/21 13:14
 */
public interface ApolloExtendListenPublish<T> extends Ordered {
    /**
     * 发布更新事件
     * @param data
     */
    void doPublish(ConfigurableApplicationContext context, T data);
}
