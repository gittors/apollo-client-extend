package com.gittors.apollo.extend.initializer;

import com.ctrip.framework.apollo.spring.boot.ApolloApplicationContextInitializer;
import com.ctrip.framework.apollo.spring.config.PropertySourcesConstants;
import com.gittors.apollo.extend.chain.spi.ChainProcessor;
import com.gittors.apollo.extend.common.constant.CommonApolloConstant;
import com.gittors.apollo.extend.common.service.ServiceLookUp;
import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 *  1、将 {@link CommonApolloConstant#APOLLO_EXTEND_NAMESPACE}
 *  配置的命名空间刷新至Spring 上下文环境
 *
 *  2、且Apollo会为其添加 {@link com.ctrip.framework.apollo.spring.property.AutoUpdateConfigChangeListener} 监听器
 *  参考：{@link com.ctrip.framework.apollo.spring.config.PropertySourcesProcessor#initializeAutoUpdatePropertiesFeature(ConfigurableListableBeanFactory)}
 *
 * @author zlliu
 * @date 2020/7/25 20:35
 */
@Slf4j
public class ApolloExtendApplicationContextInitializer implements
        ApplicationContextInitializer<ConfigurableApplicationContext>, EnvironmentPostProcessor, Ordered {

    private static final String APOLLO_PLUGIN_ENABLED = "apollo.client.extend.plugin.enabled";

    private static final String[] APOLLO_SYSTEM_PROPERTIES = {"env"};

    private static final ChainProcessor chainProcessor = ServiceLookUp.loadPrimary(ChainProcessor.class);

    public static final int DEFAULT_ORDER = ApolloApplicationContextInitializer.DEFAULT_ORDER + 100;

    private int order = DEFAULT_ORDER;

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        ConfigurableEnvironment environment = applicationContext.getEnvironment();
        if (!environment.getProperty(APOLLO_PLUGIN_ENABLED, Boolean.class, Boolean.TRUE)) {
            log.debug("Apollo config is not enabled for context {}, see property: ${{}}", applicationContext, APOLLO_PLUGIN_ENABLED);
            return;
        }
        //  之所以用Apollo Bootstrap配置，是因为不配置这个开关，则意味着不会加载远端的application配置，注册NameSpace就无从谈起了
        if (!environment.getProperty(PropertySourcesConstants.APOLLO_BOOTSTRAP_ENABLED, Boolean.class, Boolean.FALSE)) {
            log.debug("Apollo bootstrap config is not enabled for context {}, see property: ${{}}", applicationContext, PropertySourcesConstants.APOLLO_BOOTSTRAP_ENABLED);
            return;
        }
        if (environment.getPropertySources().contains(CommonApolloConstant.APOLLO_BOOTSTRAP_PROPERTY_SOURCE_NAME)) {
            //  already initialized
            return;
        }
        initialize(environment);
    }

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment configurableEnvironment, SpringApplication springApplication) {
        //  initializeSystemProperty
        for (String propertyName : APOLLO_SYSTEM_PROPERTIES) {
            if (System.getProperty(propertyName) != null) {
                return;
            }
            String propertyValue = configurableEnvironment.getProperty(propertyName);
            if (Strings.isNullOrEmpty(propertyValue)) {
                return;
            }
            System.setProperty(propertyName, propertyValue);
        }
    }

    private void initialize(ConfigurableEnvironment environment) {
        try {
            //  解析管理命名空间并注册到Spring环境
            chainProcessor.process(environment, "namespace injector", null);
        } catch (Throwable throwable) {
            log.error("#initialize error: ", throwable);
        }
    }

    @Override
    public int getOrder() {
        return order;
    }
}
