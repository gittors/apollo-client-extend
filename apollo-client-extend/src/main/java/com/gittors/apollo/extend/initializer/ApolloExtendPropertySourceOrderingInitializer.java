package com.gittors.apollo.extend.initializer;

import com.ctrip.framework.apollo.spring.config.PropertySourcesConstants;
import com.gittors.apollo.extend.common.constant.CommonApolloConstant;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;

/**
 * 将 Apollo Extend的 PropertySources排序
 *
 * @author zlliu
 * @date 2022/08/22 23:31
 */
public class ApolloExtendPropertySourceOrderingInitializer implements
        ApplicationContextInitializer<ConfigurableApplicationContext>, Ordered {

    public static final int DEFAULT_ORDER = ApolloExtendApplicationContextInitializer.DEFAULT_ORDER + 100;

    private int order = DEFAULT_ORDER;

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        ConfigurableEnvironment environment = applicationContext.getEnvironment();
        MutablePropertySources propertySources = environment.getPropertySources();
        if (!propertySources.contains(CommonApolloConstant.APOLLO_BOOTSTRAP_PROPERTY_SOURCE_NAME)) {
            return;
        }
        //  将PropertySources 排序在 ApolloBootstrapPropertySources 后面
        CompositePropertySource bootstrapComposite = (CompositePropertySource) propertySources.remove(CommonApolloConstant.APOLLO_BOOTSTRAP_PROPERTY_SOURCE_NAME);
        if (propertySources.contains(PropertySourcesConstants.APOLLO_BOOTSTRAP_PROPERTY_SOURCE_NAME)) {
            propertySources.addAfter(PropertySourcesConstants.APOLLO_BOOTSTRAP_PROPERTY_SOURCE_NAME, bootstrapComposite);
        } else {
            propertySources.addAfter(PropertySourcesConstants.APOLLO_PROPERTY_SOURCE_NAME, bootstrapComposite);
        }
    }

    @Override
    public int getOrder() {
        return order;
    }
}
