package com.gittors.apollo.extend.common.constant;

/**
 * @author zlliu
 * @date 2020/8/24 17:21
 */
public class ApolloExtendAdminConstant {

    public static final String OK = "OK";

    /**
     * 缓存大小
     */
    public static final String CACHE_SIZE = "cache.size";
    /**
     * 缓存时长
     */
    public static final String CACHE_DURATION = "cache.duration";

    /**
     * 缓存单位配置
     */
    public static final String TIME_UNIT_STR = "cache.str";

    /**
     * webflux 配置开关
     */
    public static final String ADMIN_WEBFLUX_AUTO_CONFIGURATION_ENABLED = "apollo.extend.admin.webflux.configuration.enabled";

    /**
     * WEB 配置开关
     */
    public static final String ADMIN_AUTO_CONFIGURATION_ENABLED = "apollo.extend.admin.web.configuration.enabled";

    /**
     * 请求拦截开关：缺省 TRUE
     */
    public static final String REQUEST_INTERCEPTOR_ENABLED = "apollo.extend.admin.request.interceptor.enabled";

    /**
     * Token 拦截开关：缺省：TRUE
     */
    public static final String TOKEN_INTERCEPTOR_ENABLED = "apollo.extend.admin.token.interceptor.enabled";
}
