package com.gittors.apollo.extend.binder.annotation;

import com.ctrip.framework.apollo.spring.util.BeanRegistrationUtil;
import com.gittors.apollo.extend.binder.processor.AutoBinderConfigListenerPostProcessor;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;

/**
 * 适配 {@link BinderScan 注解}
 *
 * @author zlliu
 * @date 2020/8/19 21:39
 */
public class BinderScannerRegistrar extends AbstractBinderRegistrar {

    @Override
    protected String getAnnotationName() {
        return BinderScan.class.getName();
    }

    @Override
    protected void registerCustomizeBeans(BeanDefinitionRegistry registry) {
        BeanRegistrationUtil.registerBeanDefinitionIfNotExists(registry, AutoBinderConfigListenerPostProcessor.class.getName(),
                AutoBinderConfigListenerPostProcessor.class);
    }
}
