package com.gittors.apollo.extend.admin.webflux.config;

import com.gittors.apollo.extend.admin.webflux.entity.ApiResponse;
import com.gittors.apollo.extend.admin.webflux.entity.DataEntity;
import com.gittors.apollo.extend.admin.webflux.handler.ServiceHandler;
import com.gittors.apollo.extend.common.constant.ApolloExtendAdminConstant;
import com.gittors.apollo.extend.common.encry.EncryptUtils;
import com.gittors.apollo.extend.common.manager.CacheManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RequestPredicates.contentType;

/**
 * @author zlliu
 * @date 2020/8/24 16:27
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(
        name = ApolloExtendAdminConstant.ADMIN_WEBFLUX_AUTO_CONFIGURATION_ENABLED,
        havingValue = "true"
)
@ConditionalOnWebApplication(type = Type.REACTIVE)
public class ApolloExtendAdminWebfluxConfiguration {

    @Configuration(proxyBeanMethods = false)
    @Import(ServiceHandler.class)
    private class WebFluxConfiguration implements WebFluxConfigurer {

        @Autowired
        @Qualifier("extendAdminWebFluxCacheManager")
        private CacheManager cacheManager;

        @Autowired
        private ServiceHandler serviceHandler;

        @Bean
        public RouterFunction<ServerResponse> extendAdminRouterFunction() {
            return RouterFunctions
                    .route(GET("/token/get").and(request -> {
                        // 不满足条件则404，限制1分钟只能访问一次
                        return cacheManager.get(request.path()) == null;
                    }), request -> {
                        String token = EncryptUtils.encrypt(request.path());
                        cacheManager.put(request.path(), "GET Token");
                        cacheManager.put(token, "Token");

                        return ServerResponse.ok().contentType(MediaType.TEXT_PLAIN)
                                .body(BodyInserters.fromValue(token));
                    })
                    .andRoute(POST("/namespace/inject-namespace")
                                    .and(contentType(APPLICATION_JSON))
                                    .and(request -> {
                                        // 不满足条件则404，限制1分钟只能访问一次
                                        return cacheManager.get(request.path()) == null;
                                    }), request -> handler(request, ServiceHandler.HandlerEnum.HANDLER_NAMESPACEINJECT)
                    )
                    .andRoute(POST("/namespace/delete-namespace")
                                    .and(contentType(APPLICATION_JSON))
                                    .and(request -> {
                                        // 不满足条件则404，限制1分钟只能访问一次
                                        return cacheManager.get(request.path()) == null;
                                    }), request -> handler(request, ServiceHandler.HandlerEnum.HANDLER_NAMESPACEDELETE)
                    )
                    ;
        }

        private Mono<ServerResponse> handler(ServerRequest request, ServiceHandler.HandlerEnum handlerEnum) {
            Mono<DataEntity> dataEntityMono = request.bodyToMono(DataEntity.class);
            return
            dataEntityMono.flatMap(dataEntity -> {
                //  1参数校验
                ApiResponse apiResponse = serviceHandler.doHandler(ServiceHandler.HandlerEnum.CHECK_PARAMETER, dataEntity);
                if (apiResponse != null) {
                    return ServerResponse.status(apiResponse.getCode()).contentType(MediaType.TEXT_PLAIN)
                            .body(BodyInserters.fromValue(apiResponse.getMsg()));
                }

                //  2命名空间操作
                serviceHandler.doHandler(handlerEnum, dataEntity);

                cacheManager.put(request.path(), "Injector Namespace");
                return ServerResponse.ok().contentType(MediaType.TEXT_PLAIN)
                        .body(BodyInserters.fromValue(ApolloExtendAdminConstant.OK));
            });
        }
    }

    private static class AdminWebFluxConfiguration {
        @Bean
        public CacheManager extendAdminWebFluxCacheManager(Environment environment) {
            return new CacheManager(environment);
        }
    }

}
