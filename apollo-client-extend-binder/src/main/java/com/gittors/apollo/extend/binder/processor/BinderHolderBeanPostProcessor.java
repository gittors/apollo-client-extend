package com.gittors.apollo.extend.binder.processor;

import com.gittors.apollo.extend.binder.context.BinderContext;
import com.gittors.apollo.extend.binder.registry.HolderBeanWrapper;
import com.gittors.apollo.extend.binder.registry.HolderBeanWrapperRegistry;
import com.gittors.apollo.extend.binder.utils.BinderObjectInjector;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * @author zlliu
 * @date 2020/8/10 21:39
 */
public class BinderHolderBeanPostProcessor implements BeanPostProcessor, BeanFactoryAware, PriorityOrdered {

    private BeanFactory beanFactory;

    private final HolderBeanWrapperRegistry beanWrapperRegistry =
            BinderObjectInjector.getInstance(HolderBeanWrapperRegistry.class);

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (MapUtils.isEmpty(BinderContext.INSTANCE.getBinderMap())) {
            return bean;
        }
        Field[] fields = bean.getClass().getDeclaredFields();
        for (Field field : fields) {
            for (Map.Entry<String, Class<?>> entry : BinderContext.INSTANCE.getBinderMap().entrySet()) {
                //  判断类的属性是否满足条件
                boolean match = field.getType() == entry.getValue();
                if (match) {
                    HolderBeanWrapper beanWrapper =
                            new HolderBeanWrapper(entry.getKey(), null, bean, beanName, field);
                    beanWrapperRegistry.register(beanFactory, entry.getKey(), beanWrapper);
                }
            }
        }
        return bean;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 10;
    }
}
