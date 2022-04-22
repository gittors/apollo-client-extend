package com.gittors.apollo.extend.support.constant;

public final class SupportConstant {

    /**
     * 代理开关
     */
    public static final String CONFIG_PROXY_SWITCH = "config.factory.proxy";

    /**
     * 代理调试开关
     */
    public static final String CONFIG_PROXY_DEBUG_SWITCH = CONFIG_PROXY_SWITCH + ".out";

    /**
     * 代理调试路径
     */
    public static final String CONFIG_PROXY_DEBUG_PATH = "target/proxy";

    public static final String APOLLO_CHANGE_LISTENER = "com.ctrip.framework.apollo.internals.RepositoryChangeListener";

    public static final String APOLLO_CLIENT_EXTEND_CONFIG = "com.gittors.apollo.extend.support.ext.ApolloClientExtendConfig";

    //  ============================= DefaultConfigExt 扩展方法 ===============================================
    public static final String DEFAULT_CONFIG_UPDATE_CONFIG_METHOD = "updateConfig";

    public static final String DEFAULT_CONFIG_INITIALIZE_METHOD = "initialize";

    public static final String DEFAULT_CONFIG_ON_REPOSITORY_CHANGE_METHOD = "onRepositoryChange";

    //  ============================= AbstractConfigExt 扩展方法 ===============================================
    public static final String ABSTRACT_CONFIG_CALC_PROPERTY_CHANGES_METHOD = "calcPropertyChanges";

    public static final String ABSTRACT_CONFIG_CONFIG_CHANGE_LISTENER = "com.ctrip.framework.apollo.ConfigChangeListener";

    public static final String ABSTRACT_CONFIG_CONFIG_CHANGE_EVENT = "com.ctrip.framework.apollo.model.ConfigChangeEvent";

    public static final String ABSTRACT_CONFIG_FIRE_CONFIG_CHANGE_METHOD = "fireConfigChange";

    //  ============================    DefaultConfigFactoryExt 扩展方法 =======================================
    public static final String DEFAULT_CONFIG_FACTORY_CREATE_METHOD = "create";
}
