package com.gittors.apollo.extend.admin.web.config;

import com.gittors.apollo.extend.admin.web.endpoint.AdminNamespaceEndpoint;
import com.gittors.apollo.extend.admin.web.endpoint.AdminTokenEndpoint;
import com.gittors.apollo.extend.admin.web.handler.RequestInterceptor;
import com.gittors.apollo.extend.admin.web.handler.TokenInterceptor;
import com.gittors.apollo.extend.common.constant.ApolloExtendAdminConstant;
import com.gittors.apollo.extend.common.manager.CacheManager;
import org.springframework.beans.BeansException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author zlliu
 * @date 2020/8/24 16:27
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(
        name = ApolloExtendAdminConstant.ADMIN_AUTO_CONFIGURATION_ENABLED,
        havingValue = "true"
)
@ConditionalOnWebApplication(type = Type.SERVLET)
@Import(SwaggerConfiguration.class)
public class ApolloExtendAdminConfiguration {

    /**
     * Admin Endpoint Configuration
     */
    private static class AdminEndpointConfiguration {
        @Bean
        public AdminNamespaceEndpoint adminNamespaceEndpoint() {
            return new AdminNamespaceEndpoint();
        }

        @Bean
        public AdminTokenEndpoint adminTokenEndpoint() {
            return new AdminTokenEndpoint();
        }
    }

    /**
     * Other Configuration
     */
    private static class AdminOtherConfiguration {
        @Bean
        @ConditionalOnMissingBean(name = ApolloExtendAdminConstant.EXTEND_ADMIN_CACHE_MANAGER)
        public CacheManager extendAdminCacheManager(Environment environment) {
            return new CacheManager(environment);
        }
    }

    @Configuration(proxyBeanMethods = false)
    public static class WebConfiguration implements WebMvcConfigurer, ApplicationContextAware {
        private ApplicationContext applicationContext;

        @Override
        public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
            this.applicationContext = applicationContext;
        }

        @Override
        public void addInterceptors(InterceptorRegistry registry) {
            if (applicationContext.containsBean(RequestInterceptor.BEAN_NAME)) {
                HandlerInterceptor requestInterceptor =
                        applicationContext.getBean(RequestInterceptor.BEAN_NAME, HandlerInterceptor.class);
                registry.addInterceptor(requestInterceptor)
                        .addPathPatterns("/config/**")
                        .addPathPatterns("/namespace/**")
                        .excludePathPatterns("/token/**")
                ;
            }
            if (applicationContext.containsBean(TokenInterceptor.BEAN_NAME)) {
                HandlerInterceptor tokenInterceptor =
                        applicationContext.getBean(TokenInterceptor.BEAN_NAME, HandlerInterceptor.class);
                registry.addInterceptor(tokenInterceptor)
                        .addPathPatterns("/token/**")
                        .excludePathPatterns("/config/**")
                        .excludePathPatterns("/namespace/**")
                ;
            }
        }
    }

    @Configuration(proxyBeanMethods = false)
    public static class InterceptorConfiguration {
        @Bean
        @ConditionalOnProperty(
                name = ApolloExtendAdminConstant.REQUEST_INTERCEPTOR_ENABLED,
                havingValue = "true",
                matchIfMissing = true
        )
        @ConditionalOnMissingBean(name = RequestInterceptor.BEAN_NAME)
        public HandlerInterceptor requestInterceptor() {
            return new RequestInterceptor();
        }

        @Bean
        @ConditionalOnProperty(
                name = ApolloExtendAdminConstant.TOKEN_INTERCEPTOR_ENABLED,
                havingValue = "true",
                matchIfMissing = true
        )
        @ConditionalOnMissingBean(name = TokenInterceptor.BEAN_NAME)
        public HandlerInterceptor tokenInterceptor() {
            return new TokenInterceptor();
        }
    }
}
