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
    public static final String ADMIN_WEBFLUX_AUTO_CONFIG_ENABLED = "apollo.extend.admin.webflux.enabled";

    /**
     * WEB 配置开关
     */
    public static final String ADMIN_AUTO_CONFIGURATION_ENABLED = "apollo.extend.admin.web.enabled";

    /**
     * 请求拦截开关：缺省 TRUE
     */
    public static final String REQUEST_INTERCEPTOR_ENABLED = "apollo.extend.admin.request.interceptor.enabled";

    /**
     * Token 拦截开关：缺省：TRUE
     */
    public static final String TOKEN_INTERCEPTOR_ENABLED = "apollo.extend.admin.token.interceptor.enabled";

    /**
     * Extend webflux cache
     */
    public static final String EXTEND_ADMIN_WEB_FLUX_CACHE_MANAGER = "extendAdminWebFluxCacheManager";

    /**
     * Extend web cache
     */
    public static final String EXTEND_ADMIN_CACHE_MANAGER = "extendAdminCacheManager";
}
