package com.gittors.apollo.extend.spi;

import com.gittors.apollo.extend.common.service.Ordered;
import org.springframework.beans.factory.BeanFactory;

import java.util.Properties;

public class DefaultApolloExtendListenPublish implements ApolloExtendListenPublish<Properties> {
    private int order = Ordered.LOWEST_PRECEDENCE;

    @Override
    public void doPublish(BeanFactory beanFactory, Properties data) {
    }

    @Override
    public int getOrder() {
        return order;
    }
}
