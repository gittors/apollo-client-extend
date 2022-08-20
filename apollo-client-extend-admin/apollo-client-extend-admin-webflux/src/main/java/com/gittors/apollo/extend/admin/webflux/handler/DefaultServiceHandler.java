package com.gittors.apollo.extend.admin.webflux.handler;

import com.gittors.apollo.extend.admin.webflux.entity.ApiResponse;
import com.gittors.apollo.extend.admin.webflux.entity.DataEntity;
import com.gittors.apollo.extend.admin.webflux.spi.ApolloExtendAdminWebfluxProcessor;
import com.gittors.apollo.extend.common.constant.ApolloExtendAdminConstant;
import com.gittors.apollo.extend.common.constant.CommonApolloConstant;
import com.gittors.apollo.extend.common.manager.CacheManager;
import com.gittors.apollo.extend.common.spi.ServiceLookUp;
import com.gittors.apollo.extend.spi.ApolloExtendNameSpaceManager;
import com.google.common.base.Splitter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
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
public class DefaultServiceHandler implements ServiceHandler {

    private static final Splitter CALL_SPLITTER =
            Splitter.on("_").omitEmptyStrings().trimResults();

    private final Splitter NAMESPACE_SPLITTER =
            Splitter.on(CommonApolloConstant.DEFAULT_SEPARATOR)
                    .omitEmptyStrings().trimResults();

    /**
     * 参数校验
     */
    private static final String CHECK = "CHECK";

    /**
     * 命名空间处理
     */
    private static final String HANDLER = "HANDLER";

    ApolloExtendAdminWebfluxProcessor<BeanFactory> extendAdminWebfluxProcessor =
            ServiceLookUp.loadPrimary(ApolloExtendAdminWebfluxProcessor.class);

    ApolloExtendNameSpaceManager extendNameSpaceManager =
            ServiceLookUp.loadPrimary(ApolloExtendNameSpaceManager.class);

    @Autowired
    @Qualifier(ApolloExtendAdminConstant.EXTEND_ADMIN_WEB_FLUX_CACHE_MANAGER)
    private CacheManager cacheManager;

    @Autowired
    private ApplicationContext applicationContext;

    private Map<String, Class<? extends Object>> callMap = new HashMap() {{
        //  参数校验类
        put(CHECK, new ParameterCheck());

        //  其他处理类
        put(HANDLER, new InjectNamespace());
    }};

    public ApiResponse doHandler(HandlerEnum handlerEnum, DataEntity request) {
        if (handlerEnum == null || request == null) {
            return ApiResponse.fail();
        }
        //  通过枚举类名，反射得到调用对象+方法名
        String callRouteName = handlerEnum.name();
        List<String> callList = CALL_SPLITTER.splitToList(callRouteName);
        if (CollectionUtils.isEmpty(callList)) {
            log.error("#handler callRouteName is invalid!");
            return ApiResponse.fail();
        }
        Object callObject = callMap.get(callList.get(0));
        if (callObject == null) {
            log.error("#handler callObject is null!");
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
                return ApiResponse.fail(HttpStatus.BAD_REQUEST.value(), "Token must not be null!");
            } else if (cacheManager.get(dataEntity.getToken()) == null) {
                return ApiResponse.fail(HttpStatus.BAD_REQUEST.value(), "Token Invalid!");
            }
            if (StringUtils.isBlank(dataEntity.getNamespace())) {
                return ApiResponse.fail(HttpStatus.BAD_REQUEST.value(), "Namespace must not be null!");
            }
            return null;
        }
    }

    class InjectNamespace {
        /**
         * 命名空间注入
         * @param dataEntity
         * @return
         */
        public ApiResponse namespaceinject(DataEntity dataEntity) {
            extendNameSpaceManager.setApplicationContext(applicationContext);

            List<String> namespaceList = NAMESPACE_SPLITTER.splitToList(dataEntity.getNamespace());
            Set<String> newNamespaceSet = new HashSet<>(namespaceList);

            Map<String, Map<String, String>> configMap = extendNameSpaceManager.getAddNamespaceConfig(newNamespaceSet);

            extendAdminWebfluxProcessor.process(applicationContext, configMap);
            return null;
        }

        /**
         * 命名空间删除
         * @param dataEntity
         * @return
         */
        public ApiResponse namespacedelete(DataEntity dataEntity) {
            extendNameSpaceManager.setApplicationContext(applicationContext);

            List<String> namespaceList = NAMESPACE_SPLITTER.splitToList(dataEntity.getNamespace());
            Set<String> newNamespaceSet = new HashSet<>(namespaceList);

            Map<String, Map<String, String>> configMap = extendNameSpaceManager.getDeleteNamespaceConfig(newNamespaceSet);

            extendAdminWebfluxProcessor.process(applicationContext, configMap);
            return null;
        }
    }

}
