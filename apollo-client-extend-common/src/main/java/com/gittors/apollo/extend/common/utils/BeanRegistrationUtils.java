package com.gittors.apollo.extend.common.utils;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;

import java.util.Map;
import java.util.Objects;

/**
 * @author zlliu
 * @date 2021/12/20 18:05
 */
public class BeanRegistrationUtils {
    public static boolean registerBeanDefinitionIfNotExists(BeanDefinitionRegistry registry, String beanName,
                                                            Class<?> beanClass) {
        return registerBeanDefinitionIfNotExists(registry, beanName, beanClass, null);
    }

    public static boolean registerBeanDefinitionIfNotExists(BeanDefinitionRegistry registry, String beanName,
                                                            Class<?> beanClass, Map<String, Object> extraPropertyValues) {
        if (registry.containsBeanDefinition(beanName)) {
            return false;
        }
        String[] candidates = registry.getBeanDefinitionNames();
        for (String candidate : candidates) {
            BeanDefinition beanDefinition = registry.getBeanDefinition(candidate);
            if (Objects.equals(beanDefinition.getBeanClassName(), beanClass.getName())) {
                return false;
            }
        }
        BeanDefinition beanDefinition = BeanDefinitionBuilder.genericBeanDefinition(beanClass)
                .getBeanDefinition();
        if (extraPropertyValues != null) {
            for (Map.Entry<String, Object> entry : extraPropertyValues.entrySet()) {
                beanDefinition.getPropertyValues()
                        .add(entry.getKey(), entry.getValue());
            }
        }
        registry.registerBeanDefinition(beanName, beanDefinition);
        return true;
    }

}
