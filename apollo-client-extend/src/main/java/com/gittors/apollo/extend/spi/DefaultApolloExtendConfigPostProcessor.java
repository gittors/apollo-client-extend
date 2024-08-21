package com.gittors.apollo.extend.spi;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.annotation.Order;

import java.util.Map;

/**
 * @author zlliu
 * @date 2024/8/20 0020 9:51
 */
@Order
public class DefaultApolloExtendConfigPostProcessor implements ApolloExtendConfigPostProcessor<Map<String, Map<String, String>>> {

    @Override
    public void postProcess(ConfigurableApplicationContext applicationContext, Map<String, Map<String, String>> data) {
    }

}
