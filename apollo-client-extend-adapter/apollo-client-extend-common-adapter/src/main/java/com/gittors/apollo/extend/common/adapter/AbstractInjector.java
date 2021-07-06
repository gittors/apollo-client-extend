package com.gittors.apollo.extend.common.adapter;

import com.ctrip.framework.apollo.exceptions.ApolloConfigException;
import com.ctrip.framework.apollo.internals.Injector;
import com.ctrip.framework.apollo.spi.ConfigFactory;
import com.ctrip.framework.apollo.spi.DefaultConfigFactory;
import com.ctrip.framework.apollo.tracer.Tracer;
import com.gittors.apollo.extend.support.ext.DefaultConfigFactoryExt;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Module;
import com.google.inject.Singleton;

import java.util.Arrays;

/**
 * @author zlliu
 * @date 2021/7/6 17:12
 */
public abstract class AbstractInjector implements Injector {
    private com.google.inject.Injector m_injector;

    public AbstractInjector() {
        try {
            Module[] modules = concat(configureModule(), apolloExtendModuleConfigure());
            m_injector = Guice.createInjector(modules);
        } catch (Throwable ex) {
            ApolloConfigException exception = new ApolloConfigException("Unable to initialize Guice Injector!", ex);
            Tracer.logError(exception);
            throw exception;
        }
    }

    /**
     * 数组合并
     * @param first
     * @param second
     * @param <T>
     * @return
     */
    public static <T> T[] concat(T[] first, T[] second) {
        T[] result = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }

    /**
     * 配置Apollo官方的Module,如果官方的client jar版本升级,必要时重写这个方法
     *
     * @return Module数组
     */
    protected abstract Module[] configureModule();

    /**
     * 配置Apollo Client Extend的Module,必要时重写此方法
     *
     * @return
     */
    protected Module[] apolloExtendModuleConfigure() {
        return new Module[] {new ApolloExtendModule()};
    }

    /**
     * 修改 {@link DefaultConfigFactory} 注入
     */
    private static class ApolloExtendModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(ConfigFactory.class).to(DefaultConfigFactoryExt.class).in(Singleton.class);
        }
    }

    @Override
    public <T> T getInstance(Class<T> clazz) {
        try {
            return m_injector.getInstance(clazz);
        } catch (Throwable ex) {
            Tracer.logError(ex);
            throw new ApolloConfigException(
                    String.format("Unable to load instance for %s!", clazz.getName()), ex);
        }
    }

    @Override
    public <T> T getInstance(Class<T> clazz, String name) {
        //Guice does not support get instance by type and name
        return null;
    }

}
