package com.gittors.apollo.extend.spi;

import com.gittors.apollo.extend.common.service.Ordered;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * @author zlliu
 * @date 2024/8/20 0020 9:46
 */
public interface ApolloExtendConfigPostProcessor<T> extends Ordered {

    /**
     * 配置有变更，客户端的处理
     *
     * 备注：此时配置已经刷新至 Spring环境，此方法留待客户端回调后续处理
     *
     * @param data
     */
    void postProcess(ConfigurableApplicationContext applicationContext, T data);
}
