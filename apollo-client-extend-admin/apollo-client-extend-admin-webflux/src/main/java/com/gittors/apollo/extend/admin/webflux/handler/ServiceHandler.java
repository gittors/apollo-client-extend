package com.gittors.apollo.extend.admin.webflux.handler;

import com.gittors.apollo.extend.admin.webflux.entity.ApiResponse;
import com.gittors.apollo.extend.admin.webflux.entity.DataEntity;

/**
 * @author zlliu
 * @date 2021/5/11 13:28
 */
@FunctionalInterface
public interface ServiceHandler {
    /**
     * 命名空间处理
     * @param handlerEnum
     * @param request
     * @return
     */
    ApiResponse doHandler(HandlerEnum handlerEnum, DataEntity request);

    public enum HandlerEnum {
        /**
         * 参数校验
         */
        CHECK_PARAMETER,

        /**
         * 注入命名空间
         */
        HANDLER_NAMESPACEINJECT,

        /**
         * 删除命名空间
         */
        HANDLER_NAMESPACEDELETE
        ;
    }
}
