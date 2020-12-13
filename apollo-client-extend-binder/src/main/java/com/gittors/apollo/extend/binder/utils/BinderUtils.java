package com.gittors.apollo.extend.binder.utils;

import com.gittors.apollo.extend.binder.registry.HolderBeanWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.core.env.Environment;

/**
 * @author zlliu
 * @date 2020/8/20 18:01
 */
@Slf4j
public class BinderUtils {

    /**
     *  绑定对象
     *
     * @param environment
     * @param holderBeanWrapper 包涵 @ConfigurationProperties 注解属性的封装类
     * @param binderPrefix  配置前缀
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
            log.warn("#binder failed: ", ex);
        }
    }

}
