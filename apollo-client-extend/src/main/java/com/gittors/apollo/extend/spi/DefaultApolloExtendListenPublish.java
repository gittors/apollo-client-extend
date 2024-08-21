package com.gittors.apollo.extend.spi;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.annotation.Order;

/**
 * 缺省空实现：基础API不应该集成事件相关组件
 * 需要关心 listen.key 变更后数据更新的，应该手动实现此接口
 *
 * @author zlliu
 * @date 2022/08/21 13:14
 */
@Order
public class DefaultApolloExtendListenPublish<T> implements ApolloExtendListenPublish<T> {
    @Override
    public void doPublish(ConfigurableApplicationContext context, T data) {
    }
}
