package com.gittors.apollo.extend.binder.utils;

import com.gittors.apollo.extend.binder.exception.BinderException;
import com.gittors.apollo.extend.binder.registry.HolderBeanWrapperRegistry;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Singleton;

/**
 * @author zlliu
 * @date 2020/8/19 21:39
 */
public class BinderObjectInjector {
    private static volatile Injector injector;
    private static final Object lock = new Object();

    private static Injector getInjector() {
        if (injector == null) {
            synchronized (lock) {
                if (injector == null) {
                    try {
                        injector = Guice.createInjector(new SpringModule());
                    } catch (Throwable ex) {
                        throw new BinderException("Unable to initialize Apollo Spring Injector!", ex);
                    }
                }
            }
        }

        return injector;
    }

    public static <T> T getInstance(Class<T> clazz) {
        try {
            return getInjector().getInstance(clazz);
        } catch (Throwable ex) {
            throw new BinderException(
                    String.format("Unable to load instance for %s!", clazz.getName()), ex);
        }
    }

    private static class SpringModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(HolderBeanWrapperRegistry.class).in(Singleton.class);
        }
    }
}
