package com.gittors.apollo.extend.spi;

import com.gittors.apollo.extend.common.service.Ordered;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * 缺省空实现：基础API不应该集成事件相关组件
 * 需要关心 listen.key 变更后数据更新的，应该手动实现此接口
 *
 * 参考：{@link BinderApolloExtendListenPublish}
 * @author zlliu
 * @date 2022/08/21 13:14
 */
public class DefaultApolloExtendListenPublish<T> implements ApolloExtendListenPublish<T> {
    private int order = Ordered.LOWEST_PRECEDENCE;

    @Override
    public void doPublish(ConfigurableApplicationContext context, T data) {
    }

    @Override
    public int getOrder() {
        return order;
    }
}
