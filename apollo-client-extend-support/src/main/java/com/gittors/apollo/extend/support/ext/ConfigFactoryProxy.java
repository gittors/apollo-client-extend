package com.gittors.apollo.extend.support.ext;

import com.ctrip.framework.apollo.core.utils.StringUtils;
import com.ctrip.framework.apollo.spi.ConfigFactory;
import com.gittors.apollo.extend.support.constant.SupportConstant;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.Modifier;
import javassist.NotFoundException;

import java.util.Properties;

/**
 * 此类通过 Javassist 创建Ext扩展对象，达到动态扩展目的
 * 静态扩展：
 *      {@link AbstractConfigExt}
 *      {@link DefaultConfigExt}
 *      {@link DefaultConfigFactoryExt}
 * 静态扩展缺陷：如果依赖的Apollo client相应类修改，则需要手动修改扩展点！
 *
 * 注意：动态扩展是动态生成字节码原理，所以动态扩展的前提是“原类的方法不能有太大的改动或改动频繁的情况”，否则动态扩展的代码也要修改，从而就丧失了动态扩展的意义了！！！
 *      比如动态扩展的方法体是写死的：{@link INITIALIZE_METHOD} , 如果原扩展类的方法改动频繁，则 {@link INITIALIZE_METHOD} 方法体内容也得修改。
 *
 * @author zlliu
 * @date 2022/03/29 12:02
 */
public class ConfigFactoryProxy {
    //  AbstractConfig 原类名
    private static final String ABSTRACT_CONFIG = "com.ctrip.framework.apollo.internals.AbstractConfig";
    //  AbstractConfig 扩展类名
    private static final String ABSTRACT_CONFIG_EXT = "com.ctrip.framework.apollo.internals.AbstractConfigExtProxy";
    private static final String ABSTRACT_CONFIG_EXT_RUNNABLE = ABSTRACT_CONFIG_EXT + "Runnable";

    //  DefaultConfig 原类名
    private static final String DEFAULT_CONFIG = "com.ctrip.framework.apollo.internals.DefaultConfig";

    //  DefaultConfig 扩展类名
    //  注意：由于 DefaultConfig 的扩展，需要 Apollo Client回调，所以扩展类包路径要保持一致，否则会出现"java.lang.IllegalAccessError"异常：
//    private static final String DEFAULT_CONFIG_EXT = "com.gittors.apollo.extend.support.ext.DefaultConfigExtProxy";
    private static final String DEFAULT_CONFIG_EXT = "com.ctrip.framework.apollo.internals.DefaultConfigExtProxy";

    //  DefaultConfigFactory 原类名
    private static final String DEFAULT_CONFIG_FACTORY = "com.ctrip.framework.apollo.spi.DefaultConfigFactory";
    //  DefaultConfigFactory 扩展类名
    private static final String DEFAULT_CONFIG_FACTORY_EXT = "com.gittors.apollo.extend.support.ext.DefaultConfigFactoryExtProxy";

    /**
     * Javassist 构建 ConfigFactory依赖项：
     * 1、构建 AbstractConfigExt   --> 效果类似静态编码 {@link AbstractConfigExt}
     * 2、构建 DefaultConfigExt    --> 效果类似静态编码 {@link DefaultConfigExt}
     * 3、构建 DefaultConfigFactoryExt --> 效果类似静态编码 {@link DefaultConfigFactoryExt}
     *
     * @return
     */
    public Class<? extends ConfigFactory> build() {
        Class<? extends ConfigFactory> configFactoryClazz = null;
        try {
            ClassPool pool = ClassPool.getDefault();

            //  1、======================    构建 AbstractConfigExt    ===============================
            CtClass abstractConfigExtCtClass = buildAbstractConfigExt(pool);

            Properties properties = System.getProperties();
            boolean debugSwitch = properties.containsKey(SupportConstant.CONFIG_PROXY_DEBUG_SWITCH) &&
                    !StringUtils.isBlank(properties.getProperty(SupportConstant.CONFIG_PROXY_DEBUG_SWITCH)) &&
                    StringUtils.equalsIgnoreCase(properties.getProperty(SupportConstant.CONFIG_PROXY_DEBUG_SWITCH), "true");
            try {
                if (debugSwitch) {
                    abstractConfigExtCtClass.writeFile(SupportConstant.CONFIG_PROXY_DEBUG_PATH);
                }
            } catch (Throwable ex) {
                ex.printStackTrace();
            }

            //  2、================  构建 DefaultConfigExt（扩展主要针对这个类）  =========================================
            CtClass configExtCtClass = buildDefaultConfigExt(pool, abstractConfigExtCtClass);
            try {
                if (debugSwitch) {
                    configExtCtClass.writeFile(SupportConstant.CONFIG_PROXY_DEBUG_PATH);
                }
            } catch (Throwable ex) {
                ex.printStackTrace();
            }

            //  3、================  构建 DefaultConfigFactoryExt  =========================================
            CtClass defaultConfigFactoryExtCtClass = pool.getAndRename(DEFAULT_CONFIG_FACTORY, DEFAULT_CONFIG_FACTORY_EXT);
            CtMethod createMethod = defaultConfigFactoryExtCtClass.getDeclaredMethod(SupportConstant.DEFAULT_CONFIG_FACTORY_CREATE_METHOD);
            createMethod.setBody(CREATE_METHOD);
            configFactoryClazz = (Class<? extends ConfigFactory>) defaultConfigFactoryExtCtClass.toClass();
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
        return configFactoryClazz;
    }

    protected CtClass buildDefaultConfigExt(ClassPool pool, CtClass abstractConfigExtCtClass) throws NotFoundException, CannotCompileException {
        //  构建 DefaultConfigExtProxy 对象
        CtClass defaultConfigExtCtClass = pool.getAndRename(DEFAULT_CONFIG, DEFAULT_CONFIG_EXT);
        //  step 1: 设置继承父类
        defaultConfigExtCtClass.setSuperclass(abstractConfigExtCtClass);
        defaultConfigExtCtClass.setInterfaces(new CtClass[]{
                pool.get(SupportConstant.APOLLO_CHANGE_LISTENER),
                pool.get(SupportConstant.APOLLO_CLIENT_EXTEND_CONFIG)
        });
        //  step 2: 新增回调属性 propertiesCallBack
        CtField propertiesCallBack = CtField.make(PROPERTIES_CALL_BACK_FIELD, defaultConfigExtCtClass);
        defaultConfigExtCtClass.addField(propertiesCallBack);

        //  step 3: 修改 updateConfig 方法修改修饰符：private ==> public
        CtMethod updateConfigMethod = defaultConfigExtCtClass.getDeclaredMethod(SupportConstant.DEFAULT_CONFIG_UPDATE_CONFIG_METHOD);
        updateConfigMethod.setModifiers(Modifier.PUBLIC);
        updateConfigMethod.setBody(UPDATE_CONFIG_METHOD);

        //  step 4: 修改 initialize 方法体,增加回调参数重置(针对step 2的参数)
        CtMethod initializeMethod = defaultConfigExtCtClass.getDeclaredMethod(SupportConstant.DEFAULT_CONFIG_INITIALIZE_METHOD);
        initializeMethod.setModifiers(Modifier.PUBLIC);
        initializeMethod.setBody(INITIALIZE_METHOD);

        //  step 5: 新增方法 addPropertiesCallBack
        CtMethod addPropertiesCallBack = CtMethod.make(ADD_PROPERTIES_CALL_BACK_METHOD, defaultConfigExtCtClass);
        defaultConfigExtCtClass.addMethod(addPropertiesCallBack);

        //  step 6: 新增方法 setProperty
        CtMethod setProperty = CtMethod.make(SET_PROPERTY_METHOD, defaultConfigExtCtClass);
        defaultConfigExtCtClass.addMethod(setProperty);

        //  step 7: 新增方法 getConfigRepository
        CtMethod getConfigRepository = CtMethod.make(GET_CONFIG_REPOSITORY_METHOD, defaultConfigExtCtClass);
        defaultConfigExtCtClass.addMethod(getConfigRepository);

        //  step 8: 修改方法 onRepositoryChange
        CtMethod onRepositoryChange = defaultConfigExtCtClass.getDeclaredMethod(SupportConstant.DEFAULT_CONFIG_ON_REPOSITORY_CHANGE_METHOD);
        onRepositoryChange.setBody(REPOSITORY_CHANGE_METHOD);

        //  将对象加载至JVM
        defaultConfigExtCtClass.toClass();

        return defaultConfigExtCtClass;
    }

    protected CtClass buildAbstractConfigExt(ClassPool pool) throws NotFoundException, CannotCompileException {
        //  复制类并重命名
        CtClass abstractConfigExtCtClass = pool.getAndRename(ABSTRACT_CONFIG, ABSTRACT_CONFIG_EXT);
        //  step 1、新增方法 getChangeListener
        CtMethod newMethod = CtMethod.make(GET_CHANGE_LISTENER_METHOD, abstractConfigExtCtClass);
        abstractConfigExtCtClass.addMethod(newMethod);

        //  step 2、开放 calcPropertyChanges 方法权限，由default ==> protected
        CtMethod calcPropertyChangesMethod = abstractConfigExtCtClass.getDeclaredMethod(SupportConstant.ABSTRACT_CONFIG_CALC_PROPERTY_CHANGES_METHOD);
        calcPropertyChangesMethod.setModifiers(Modifier.PROTECTED);

        //  构建Runnable类
        //  {@link AbstractConfigExt#fireConfigChange} 不支持 Runnable内部类
        CtClass abstractRunnable = pool.makeClass(ABSTRACT_CONFIG_EXT_RUNNABLE);
        abstractRunnable.setInterfaces(new CtClass[]{
                pool.get("java.lang.Runnable")
        });

        //  增加属性
        CtField logger = CtField.make(LOGGER, abstractRunnable);
        abstractRunnable.addField(logger);

        CtField listener = CtField.make(LISTENER, abstractRunnable);
        abstractRunnable.addField(listener);

        CtField changeEvent = CtField.make(CONFIG_CHANGE_EVENT, abstractRunnable);
        abstractRunnable.addField(changeEvent);

        //  增加构造函数
        CtConstructor constructor = new CtConstructor(new CtClass[]{
                pool.get(SupportConstant.ABSTRACT_CONFIG_CONFIG_CHANGE_LISTENER),
                pool.get(SupportConstant.ABSTRACT_CONFIG_CONFIG_CHANGE_EVENT)
        }, abstractRunnable);
        constructor.setModifiers(Modifier.PUBLIC);
        constructor.setBody(ABSTRACT_CONFIG_PROXY_CONSTRUCTOR);
        abstractRunnable.addConstructor(constructor);

        //  增加 Runnable 函数
        CtMethod runMethod = CtNewMethod.make(ABSTRACT_CONFIG_PROXY_RUN, abstractRunnable);
        abstractRunnable.addMethod(runMethod);
        abstractRunnable.toClass();

        //  1.7.0和1.9.2版本的 fireConfigChange 方法不同，如果要兼容需要适配修改：
        //  findMatchedConfigChangeListeners 方法，如果找到了，重命名；没找到就新增相同方法
        //  notifyAsync 方法，如果找到了，重命名；没找到就新增相同方法

        //  修改 fireConfigChange 方法体：Javassist不支持 Runnable 内部类，所以需要修改方法体内容
        //  在 onRepositoryChange 方法回调时会用到
        CtMethod fireConfigChange = abstractConfigExtCtClass.getDeclaredMethod(
                SupportConstant.ABSTRACT_CONFIG_FIRE_CONFIG_CHANGE_METHOD,
                new CtClass[]{
                        pool.get(SupportConstant.ABSTRACT_CONFIG_CONFIG_CHANGE_EVENT)
                });
        fireConfigChange.setBody(FIRE_CONFIG_CHANGE_METHOD);

        //  将对象加载至JVM
        abstractConfigExtCtClass.toClass();
        return abstractConfigExtCtClass;
    }

    //  ========================================= AbstractConfig 扩展 ===================================================
    //  getChangeListener方法体
    private static final String GET_CHANGE_LISTENER_METHOD = "public java.util.List/*<com.ctrip.framework.apollo.ConfigChangeListener>*/ getChangeListener() {return com.google.common.collect.Lists.newCopyOnWriteArrayList(m_listeners);}";

    //  fireConfigChange方法体
    //  Javassist不支持Runnable内部类写法：抽出来一个类实现
    //  Javassist不支持 for(String s:list){}写法
    private static final String FIRE_CONFIG_CHANGE_METHOD = "{\n" +
            "    for (java.util.Iterator/*<com.ctrip.framework.apollo.ConfigChangeListener>*/ iterator = m_listeners.iterator(); iterator.hasNext();) {\n" +
            "      com.ctrip.framework.apollo.ConfigChangeListener listener = iterator.next();\n" +
            "      if (isConfigChangeListenerInterested(listener, $1)) {\n" +
            "        m_executorService.submit(new " + ABSTRACT_CONFIG_EXT_RUNNABLE + "(listener, $1));\n" +
            "      }\n" +
            "    }\n" +
            "}";

    //  fireConfigChange 方法体内部Runnable
    private static final String ABSTRACT_CONFIG_PROXY_RUN = "public void run(){\n" +
            "          java.lang.String listenerName = listener.getClass().getName();\n" +
            "          com.ctrip.framework.apollo.tracer.spi.Transaction transaction = com.ctrip.framework.apollo.tracer.Tracer.newTransaction(\"Apollo.ConfigChangeListener\", listenerName);\n" +
            "          try {\n" +
            "            listener.onChange(changeEvent);\n" +
            "            transaction.setStatus(com.ctrip.framework.apollo.tracer.spi.Transaction.SUCCESS);\n" +
            "          } catch (java.lang.Throwable ex) {\n" +
            "            transaction.setStatus(ex);\n" +
            "            com.ctrip.framework.apollo.tracer.Tracer.logError(ex);\n" +
            "            logger.error(\"Failed to invoke config change listener {}\", listenerName, ex);\n" +
            "          } finally {\n" +
            "            transaction.complete();\n" +
            "          }\n" +
            "}";
    //  内部线程类构造函数
    private static final String ABSTRACT_CONFIG_PROXY_CONSTRUCTOR = "{this.listener=$1;this.changeEvent=$2;}";
    //  logger属性
    private static final String LOGGER = "private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(" + ABSTRACT_CONFIG_EXT_RUNNABLE + ".class);";
    //  listener属性
    private static final String LISTENER = "private final com.ctrip.framework.apollo.ConfigChangeListener listener;";
    //  内部线程类 ConfigChangeEvent 对象属性
    private static final String CONFIG_CHANGE_EVENT = "private final com.ctrip.framework.apollo.model.ConfigChangeEvent changeEvent;";

    //  ========================================= AbstractConfig 扩展END ================================================

    //  ========================================= DefaultConfig 扩展 ===================================================
    //  propertiesCallBack 属性
    private static final String PROPERTIES_CALL_BACK_FIELD = "private final java.util.concurrent.atomic.AtomicReference/*<com.gittors.apollo.extend.support.ext.PropertiesCallBack>*/ propertiesCallBack = new java.util.concurrent.atomic.AtomicReference/*<>*/();";
    //  updateConfig 方法
    //  $1=arg1 $2=arg2
    private static final String UPDATE_CONFIG_METHOD = "{synchronized(this){m_configProperties.set($1);m_sourceType = $2;}}";
    //  initialize 方法体
    private static final String INITIALIZE_METHOD = "{" +
            "try {" +
            "   updateConfig(m_configRepository.getConfig(), m_configRepository.getSourceType());" +
            "} catch (Throwable ex) {} " +
            "finally {" +
            "   m_configRepository.addChangeListener(this);" +
            "   // ++增加callback回调重置" +
            "   propertiesCallBack.set(null);" +
            "}" +
        "}";
    //  addPropertiesCallBack 方法体
    private static final String ADD_PROPERTIES_CALL_BACK_METHOD = "public synchronized void addPropertiesCallBack(com.gittors.apollo.extend.support.ext.PropertiesCallBack callBack) {this.propertiesCallBack.set(callBack);}";
    //  setProperty 方法体
    private static final String SET_PROPERTY_METHOD = "public synchronized void setProperty(java.lang.String key, java.lang.String value) {" +
            "if (m_configProperties.get() != null) {" +
            "((java.util.Properties)m_configProperties.get()).setProperty(key, value);" +
            "}}";
    //  getConfigRepository 方法体
    private static final String GET_CONFIG_REPOSITORY_METHOD = "public com.ctrip.framework.apollo.internals.ConfigRepository getConfigRepository() {" +
            "return $0.m_configRepository;}";
    //  onRepositoryChange 方法体
    //  SET Body：参数要用 Javassist语法$指定；方法体要用{}包含
    //  参数设置：$1=arg1 $2=arg2
    //  引用外部类要包含全类名，如：java.util.Map
    //  泛型要用/**/标记，如：java.util.Map/*<String, ConfigChange>*/
    private static final String REPOSITORY_CHANGE_METHOD = "{\n" +
            "    if ($2.equals(m_configProperties.get())) {\n" +
            "      return ;\n" +
            "    }\n" +
            "    //  ++新增配置回调逻辑\n" +
            "    if (propertiesCallBack.get() != null) {\n" +
            "      $2 = (java.util.Properties) ((com.gittors.apollo.extend.support.ext.PropertiesCallBack)propertiesCallBack.get()).callBack($2);\n" +
            "    }\n" +
            "    com.ctrip.framework.apollo.enums.ConfigSourceType sourceType = m_configRepository.getSourceType();\n" +
            "    java.util.Properties newConfigProperties = new java.util.Properties();\n" +
            "    newConfigProperties.putAll($2);\n" +
            "    java.util.Map/*<String, com.ctrip.framework.apollo.model.ConfigChange>*/ actualChanges = updateAndCalcConfigChanges(newConfigProperties, sourceType);\n" +
            "    //check double checked result\n" +
            "    if (actualChanges.isEmpty()) {\n" +
            "      return ;\n" +
            "    }\n" +
            "    this.fireConfigChange(new com.ctrip.framework.apollo.model.ConfigChangeEvent(m_namespace, actualChanges));\n" +
            "    com.ctrip.framework.apollo.tracer.Tracer.logEvent(\"Apollo.Client.ConfigChanges\", m_namespace);\n" +
            "}";
    //  ========================================= DefaultConfig 扩展END =================================================

    //  ========================================= DefaultConfigFactory 扩展 =============================================
    //  DefaultConfigExtProxy 对象是上面build方法通过 Javassist创建的对象
    private static final String CREATE_METHOD = "{" +
            "com.ctrip.framework.apollo.core.enums.ConfigFileFormat format = determineFileFormat($1);\n" +
            "        if (com.ctrip.framework.apollo.core.enums.ConfigFileFormat.isPropertiesCompatible(format)) {\n" +
            "            return new " + DEFAULT_CONFIG_EXT + "($1, createPropertiesCompatibleFileConfigRepository($1, format));\n" +
            "        }\n" +
            "        return new " + DEFAULT_CONFIG_EXT + "($1, createLocalConfigRepository($1));" +
            "}";
}
