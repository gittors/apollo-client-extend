package com.gittors.apollo.extend.admin.webflux.handler;

import com.gittors.apollo.extend.admin.webflux.entity.ApiResponse;
import com.gittors.apollo.extend.admin.webflux.entity.DataEntity;
import com.gittors.apollo.extend.admin.webflux.spi.ApolloExtendAdminWebfluxProcessor;
import com.gittors.apollo.extend.common.constant.CommonApolloConstant;
import com.gittors.apollo.extend.common.spi.ServiceLookUp;
import com.gittors.apollo.extend.common.manager.CacheManager;
import com.gittors.apollo.extend.spi.ApolloExtendNameSpaceManager;
import com.google.common.base.Splitter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.http.HttpStatus;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author zlliu
 * @date 2020/8/27 16:53
 */
@Slf4j
public class ServiceHandler {

    private static final Splitter CALL_SPLITTER =
            Splitter.on("_").omitEmptyStrings().trimResults();

    private final Splitter NAMESPACE_SPLITTER =
            Splitter.on(CommonApolloConstant.DEFAULT_SEPARATOR)
                    .omitEmptyStrings().trimResults();

    private static final String CHECK = "CHECK";

    private static final String HANDLER = "HANDLER";

    ApolloExtendAdminWebfluxProcessor<BeanFactory> extendAdminWebfluxProcessor =
            ServiceLookUp.loadPrimary(ApolloExtendAdminWebfluxProcessor.class);

    ApolloExtendNameSpaceManager extendNameSpaceManager =
            ServiceLookUp.loadPrimary(ApolloExtendNameSpaceManager.class);

    @Autowired
    @Qualifier("extendAdminWebFluxCacheManager")
    private CacheManager cacheManager;

    @Autowired
    private BeanFactory beanFactory;

    @Autowired
    private ConfigurableEnvironment environment;

    private Map<String, Class<? extends Object>> callMap = new HashMap() {{
        put(CHECK, new ParameterCheck());
        put(HANDLER, new InjectNamespace());
    }};

    public ApiResponse doHandler(HandlerEnum handlerEnum, DataEntity request) {
        if (handlerEnum == null || request == null) {
            return ApiResponse.fail();
        }
        String callRouteName = handlerEnum.name();
        List<String> callList = CALL_SPLITTER.splitToList(callRouteName);
        if (CollectionUtils.isEmpty(callList)) {
            return ApiResponse.fail();
        }
        Object callObject = callMap.get(callList.get(0));
        if (callObject == null) {
            return ApiResponse.fail();
        }
        try {
            Class clazz = callObject.getClass();
            String methodName = callList.get(1);
            if (StringUtils.isNotBlank(methodName)) {
                Method method = clazz.getDeclaredMethod(methodName.toLowerCase(), DataEntity.class);
                return (ApiResponse) method.invoke(callObject, request);
            }
        } catch (Exception e) {
            log.error("#handler error: ", e);
            return ApiResponse.fail(e.getMessage());
        }
        return ApiResponse.fail();
    }

    class ParameterCheck {
        public ApiResponse parameter(DataEntity dataEntity) {
            if (StringUtils.isBlank(dataEntity.getToken())) {
                return ApiResponse.fail(HttpStatus.BAD_REQUEST.value(), "token must not be null!");
            } else if (cacheManager.get(dataEntity.getToken()) == null) {
                return ApiResponse.fail(HttpStatus.BAD_REQUEST.value(), "Token Invalid!");
            }
            if (StringUtils.isBlank(dataEntity.getNamespace())) {
                return ApiResponse.fail(HttpStatus.BAD_REQUEST.value(), "namespace must not be null!");
            }
            return null;
        }
    }

    class InjectNamespace {
        public ApiResponse namespaceinject(DataEntity dataEntity) {
            extendNameSpaceManager.setBeanFactory(beanFactory);
            extendNameSpaceManager.setEnvironment(environment);

            List<String> namespaceList = NAMESPACE_SPLITTER.splitToList(dataEntity.getNamespace());
            Set<String> newNamespaceSet = new HashSet<>(namespaceList);

            Map<String, Map<String, String>> configMap = extendNameSpaceManager.getAddNamespace(newNamespaceSet);

            extendAdminWebfluxProcessor.process(beanFactory, configMap);
            return null;
        }

        public ApiResponse namespacedelete(DataEntity dataEntity) {
            extendNameSpaceManager.setBeanFactory(beanFactory);
            extendNameSpaceManager.setEnvironment(environment);

            List<String> namespaceList = NAMESPACE_SPLITTER.splitToList(dataEntity.getNamespace());
            Set<String> newNamespaceSet = new HashSet<>(namespaceList);

            Map<String, Map<String, String>> configMap = extendNameSpaceManager.getDeleteNamespace(newNamespaceSet);

            extendAdminWebfluxProcessor.process(beanFactory, configMap);
            return null;
        }
    }

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
