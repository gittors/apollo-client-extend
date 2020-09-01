package com.gittors.apollo.extend.common.constant;

/**
 * @author zlliu
 * @date 2020/8/19 20:06
 */
public class CommonApolloConstant {

    /**
     * apollo扩展命名空间配置前缀
     * 缺省：{@link #APOLLO_EXTEND_NAMESPACE}
     */
    public static final String APOLLO_EXTEND_NAMESPACE_PREFIX = "apollo.extend.namespace.prefix";

    /**
     * apollo扩展命名空间
     */
    public static final String APOLLO_EXTEND_NAMESPACE = "apollo.extend.namespace";

    /**
     * propertySource配置前缀
     */
    public static final String PROPERTY_SOURCE_CONFIG_SUFFIX = "apollo.extend.propertyMap.prefix";

    /**
     * propertySource 名称后缀配置Key
     */
    public static final String PROPERTY_SOURCE_CONFIG_DEFAULT_SUFFIX = "apollo.extend";

    /**
     * 全局监听Key 配置前缀
     */
    public static final String APOLLO_EXTEND_GLOBAL_LISTEN_KEY_SUFFIX = "listen.key.global";

    /**
     * 局部监听Key 配置前缀
     */
    public static final String APOLLO_EXTEND_LISTEN_KEY_SUFFIX = "listen.key";

    /**
     * 配置文件名称后缀
     */
    public static final String APOLLO_CONFIG_FILE_SUFFIX = ".properties";

    /**
     * 分隔符
     */
    public static final String DEFAULT_SEPARATOR = ",";

    /**
     * ApolloExtendCallback 缺省实现
     */
    public static final String DEFAULT_APOLLO_EXTEND_CALLBACK_ADAPTER = "defaultApolloExtendCallbackAdapter";

    public static final String NAMESPACE_APPLICATION = "application";

    public static final String CLUSTER_NAMESPACE_SEPARATOR = "+";

}
