package com.gittors.apollo.extend.support.ext;

import com.ctrip.framework.apollo.core.utils.StringUtils;
import com.ctrip.framework.apollo.exceptions.ApolloConfigException;
import com.ctrip.framework.apollo.internals.DefaultInjector;
import com.ctrip.framework.apollo.internals.Injector;
import com.ctrip.framework.apollo.spi.ConfigFactory;
import com.ctrip.framework.apollo.spi.DefaultConfigFactory;
import com.ctrip.framework.apollo.tracer.Tracer;
import com.gittors.apollo.extend.support.constant.SupportConstant;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Module;
import com.google.inject.Singleton;
import com.google.inject.util.Modules;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.Modifier;

import java.util.Properties;

/**
 * @author zlliu
 * @date 2021/11/17 22:13
 */
public class DefaultInjectorExt implements Injector {
    //  Apollo 默认注入器
    private static final String DEFAULT_INJECTOR = "com.ctrip.framework.apollo.internals.DefaultInjector";
    //  Apollo 默认注入器的module配置类
    private static final String DEFAULT_INJECTOR_MODULE = "com.ctrip.framework.apollo.internals.DefaultInjector$ApolloModule";
    //  Apollo 默认注入器的module配置代理类
    private static final String DEFAULT_INJECTOR_MODULE_PROXY = "com.gittors.apollo.extend.support.ext.ApolloModuleProxy";
    //  Apollo 默认注入器的module配置类方法名
    private static final String DEFAULT_INJECTOR_MODULE_CONFIGURE = DEFAULT_INJECTOR_MODULE + ".configure()";

    private com.google.inject.Injector m_injector;

    public DefaultInjectorExt() {
        try {
            //  Guice覆盖绑定: https://cloud.tencent.com/developer/ask/78703
            m_injector = Guice.createInjector(Modules.override(buildModule()).with(new ApolloExtendModule()));
        } catch (Throwable ex) {
            ApolloConfigException exception = new ApolloConfigException("Unable to initialize Guice Injector!", ex);
            Tracer.logError(exception);
            throw exception;
        }
    }

    /**
     * 通过Javassist扩展 {@link DefaultInjector}
     * 1、找到 {@link DefaultInjector} 的内部类 ApolloModule
     * 2、根据 ApolloModule 构建一个类 ApolloModuleProxy
     * 3、找到 ApolloModule 的configure方法并添加至 ApolloModuleProxy
     * 4、根据 ApolloModuleProxy 创建一个对象返回
     *
     * 目的：ApolloModule 的configure方法如果增加绑定项，则将其动态添加到 ApolloModuleProxy
     *
     * @return
     * @throws Throwable
     */
    protected Module buildModule() throws Throwable {
        ClassPool pool = ClassPool.getDefault();
        //  获得 CtClass 对象
        CtClass ctClass = pool.get(DEFAULT_INJECTOR);
        //  获得所有内部类
        CtClass[] innerClass = ctClass.getNestedClasses();
        for (CtClass innerCtClazz : innerClass) {
            //  找到内部类 ApolloModule
            if (DEFAULT_INJECTOR_MODULE.equals(innerCtClazz.getName())) {
                //  根据ApolloModule构建一个类
                CtClass proxyCtClass = pool.makeClass(DEFAULT_INJECTOR_MODULE_PROXY);
                proxyCtClass.setModifiers(Modifier.PUBLIC);
                //  添加父类及接口
                proxyCtClass.setSuperclass(pool.get("com.google.inject.AbstractModule"));
                proxyCtClass.setInterfaces(new CtClass[]{
                        pool.get("com.google.inject.Module")
                });
                //  添加构造方法
                CtConstructor ctConstructor = new CtConstructor(new CtClass[]{}, proxyCtClass);
                ctConstructor.setModifiers(Modifier.PUBLIC);
                ctConstructor.setBody("{}");
                proxyCtClass.addConstructor(ctConstructor);
                //  遍历内部类方法
                for (CtMethod method : innerCtClazz.getMethods()) {
                    //  找到ApolloModule的configure方法，必须全路径
                    if (method.getLongName().equals(DEFAULT_INJECTOR_MODULE_CONFIGURE)) {
                        //  复制configure方法，添加至agentClass
                        CtMethod copy = CtNewMethod.copy(method, proxyCtClass, null);
                        copy.setName(method.getName());
                        proxyCtClass.addMethod(copy);
                    }
                }
                Class agentClazz = proxyCtClass.toClass();
                return (Module) agentClazz.newInstance();
            }
        }
        return new AbstractModule() {
            @Override
            protected void configure() {
            }
        };
    }

    /**
     * 修改 {@link ConfigFactory} 注入：
     *      {@link DefaultConfigFactory} --> {@link DefaultConfigFactoryExt}
     */
    private class ApolloExtendModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(ConfigFactory.class).to(buildFactory()).in(Singleton.class);
        }
    }

    protected Class<? extends ConfigFactory> buildFactory() {
        Class<? extends ConfigFactory> clazz = DefaultConfigFactoryExt.class;
        Properties properties = System.getProperties();
        //  Javassist配置：默认动态扩展，预留开关控制
        if (!properties.containsKey(SupportConstant.CONFIG_PROXY_SWITCH) ||
                (!StringUtils.isBlank(properties.getProperty(SupportConstant.CONFIG_PROXY_SWITCH)) &&
                        StringUtils.equalsIgnoreCase(properties.getProperty(SupportConstant.CONFIG_PROXY_SWITCH), "true"))) {
            if ((clazz = new ConfigFactoryProxy().build()) == null) {
                clazz = DefaultConfigFactoryExt.class;
            }
        }
        return clazz;
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
    public <T> T getInstance(Class<T> aClass, String s) {
        return null;
    }
}
