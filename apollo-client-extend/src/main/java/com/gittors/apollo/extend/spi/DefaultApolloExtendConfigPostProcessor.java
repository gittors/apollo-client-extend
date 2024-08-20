package com.gittors.apollo.extend.spi;

import com.gittors.apollo.extend.common.service.Ordered;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Map;

/**
 * @author zlliu
 * @date 2024/8/20 0020 9:51
 */
public class DefaultApolloExtendConfigPostProcessor implements ApolloExtendConfigPostProcessor<Map<String, Map<String, String>>> {

    @Override
    public void postProcess(ConfigurableApplicationContext applicationContext, Map<String, Map<String, String>> data) {
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
