package com.gittors.apollo.extend.support.ext;

import com.ctrip.framework.apollo.exceptions.ApolloConfigException;
import com.ctrip.framework.apollo.internals.DefaultInjector;
import com.ctrip.framework.apollo.internals.Injector;
import com.ctrip.framework.apollo.spi.ConfigFactory;
import com.ctrip.framework.apollo.spi.DefaultConfigFactory;
import com.ctrip.framework.apollo.tracer.Tracer;
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

/**
 * @author zlliu
 * @date 2021/11/17 22:13
 */
public class DefaultInjectorExt implements Injector {
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
     * 2、根据 ApolloModule 构建一个类
     * 3、找到 ApolloModule 的configure方法并添加至2
     * 4、根据2的类创建一个对象返回
     *
     * @return
     * @throws Throwable
     */
    private static Module buildModule() throws Throwable {
        ClassPool pool = ClassPool.getDefault();
        CtClass ctClass = pool.get("com.ctrip.framework.apollo.internals.DefaultInjector");
        //  找出所有内部类
        CtClass[] innerClass = ctClass.getNestedClasses();
        for (CtClass clazz : innerClass) {
            //  找到内部类 ApolloModule
            if ("com.ctrip.framework.apollo.internals.DefaultInjector$ApolloModule".equals(clazz.getName())) {
                //  根据ApolloModule构建一个类
                CtClass agentClass = pool.makeClass("com.gittors.apollo.extend.support.ext.ApolloModuleAgent");
                agentClass.setModifiers(Modifier.PUBLIC);
                //  添加父类及接口
                agentClass.setSuperclass(pool.get("com.google.inject.AbstractModule"));
                agentClass.setInterfaces(new CtClass[]{
                        pool.get("com.google.inject.Module")
                });
                CtConstructor ctConstructor = new CtConstructor(new CtClass[]{}, agentClass);
                ctConstructor.setModifiers(Modifier.PUBLIC);
                ctConstructor.setBody("{}");
                agentClass.addConstructor(ctConstructor);
                for (CtMethod method : clazz.getMethods()) {
                    //  找到ApolloModule的configure方法
                    if (method.getLongName().equals("com.ctrip.framework.apollo.internals.DefaultInjector$ApolloModule.configure()")) {
                        CtMethod copy = CtNewMethod.copy(method, agentClass, null);
                        copy.setName(method.getName());
                        agentClass.addMethod(copy);
                    }
                }
                Class aClass = agentClass.toClass();
                return (Module) aClass.newInstance();
            }
        }
        return null;
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
    public <T> T getInstance(Class<T> aClass, String s) {
        return null;
    }
}
