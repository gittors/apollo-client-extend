package com.gittors.apollo.extend.common.constant;

/**
 * @author zlliu
 * @date 2020/8/19 20:06
 */
public class CommonApolloConstant {

    /**
     * Apollo扩展命名空间配置前缀
     * 缺省：{@link #APOLLO_EXTEND_NAMESPACE}
     */
    public static final String APOLLO_EXTEND_NAMESPACE_PREFIX = "apollo.extend.namespace.prefix";

    /**
     * Apollo扩展命名空间
     */
    public static final String APOLLO_EXTEND_NAMESPACE = "apollo.extend.namespace";

    /**
     * propertySource 名称后缀缺省值
     */
    public static final String APOLLO_EXTEND_PROPERTY_SOURCE_NAME = "PropertySource";

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

    /**
     * application 命名空间
     */
    public static final String NAMESPACE_APPLICATION = "application";

    public static final String CLUSTER_NAMESPACE_SEPARATOR = "+";

    /**
     * 管理配置：新增命名空间时，生效的配置属性
     */
    public static final String APOLLO_EXTEND_ADD_CALLBACK_CONFIG = APOLLO_EXTEND_LISTEN_KEY_SUFFIX + ".addMap";

    /**
     * 管理配置：删除命名空间时，失效的配置属性
     */
    public static final String APOLLO_EXTEND_DELETE_CALLBACK_CONFIG = APOLLO_EXTEND_LISTEN_KEY_SUFFIX + ".delMap";

    /**
     * 配置的 PropertySources
     */
    public static final String APOLLO_BOOTSTRAP_PROPERTY_SOURCE_NAME = "ApolloExtendBootstrapPropertySources";

    public static final String ADMIN_ENDPOINT_PROPERTY_SOURCES_NAME = "AdminEndpointPropertySources";

    /**
     * 精确属性过滤开关
     */
    public static final String APOLLO_PROPERTY_FILTER_ENABLE = "apollo.property.filter.enabled";

}
