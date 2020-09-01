package com.gittors.apollo.extend.binder.utils;

import com.gittors.apollo.extend.binder.registry.HolderBeanWrapper;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.core.env.Environment;

/**
 * @author zlliu
 * @date 2020/8/20 18:01
 */
public class BinderUtils {

    /**
     *
     * @param environment
     * @param holderBeanWrapper 包涵 @ConfigurationProperties 注解属性的封装类
     * @param binderPrefix
     */
    public static void binder(Environment environment,
                              HolderBeanWrapper holderBeanWrapper, String binderPrefix) {
        try {
            Binder binder = Binder.get(environment);
            Object value = binder.bind(binderPrefix,
                    Bindable.of(holderBeanWrapper.getField().getType()))
                    .orElse(null);
            if (value != null) {
                holderBeanWrapper.update(value);
            }

        } catch (Throwable ex) {
        }
    }

}
