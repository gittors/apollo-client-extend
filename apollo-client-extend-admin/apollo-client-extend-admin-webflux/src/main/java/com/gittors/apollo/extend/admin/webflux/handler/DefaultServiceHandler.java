package com.gittors.apollo.extend.admin.webflux.handler;

import com.gittors.apollo.extend.admin.webflux.entity.ApiResponse;
import com.gittors.apollo.extend.admin.webflux.entity.DataEntity;
import com.gittors.apollo.extend.admin.webflux.spi.ApolloExtendAdminWebfluxProcessor;
import com.gittors.apollo.extend.common.constant.ApolloExtendAdminConstant;
import com.gittors.apollo.extend.common.constant.CommonApolloConstant;
import com.gittors.apollo.extend.common.env.SimplePropertySource;
import com.gittors.apollo.extend.common.manager.CacheManager;
import com.gittors.apollo.extend.common.service.CommonServiceLoader;
import com.gittors.apollo.extend.env.SimpleCompositePropertySource;
import com.gittors.apollo.extend.spi.ApolloExtendNameSpaceManager;
import com.gittors.apollo.extend.support.ext.ApolloClientExtendConfig;
import com.gittors.apollo.extend.utils.ApolloExtendUtils;
import com.google.common.base.Splitter;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.http.HttpStatus;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
            CommonServiceLoader.loadPrimary(ApolloExtendAdminWebfluxProcessor.class);

    ApolloExtendNameSpaceManager extendNameSpaceManager =
            CommonServiceLoader.loadPrimary(ApolloExtendNameSpaceManager.class);

    @Autowired
    @Qualifier(ApolloExtendAdminConstant.EXTEND_ADMIN_WEB_FLUX_CACHE_MANAGER)
    private CacheManager cacheManager;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

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
         *
         * @param dataEntity
         * @return
         */
        public ApiResponse namespaceinject(DataEntity dataEntity) {
            extendNameSpaceManager.setApplicationContext(applicationContext);

            List<String> namespaceList = NAMESPACE_SPLITTER.splitToList(dataEntity.getNamespace());
            Set<String> newNamespaceSet = new HashSet<>(namespaceList);

            Map<String, Map<String, String>> configMap = extendNameSpaceManager.getAddNamespaceConfig(newNamespaceSet);

            extendAdminWebfluxProcessor.process(applicationContext, configMap, "ApolloExtendAdminWebFlux.DefaultServiceHandler#namespaceinject");
            injectPostHandler(newNamespaceSet);
            return null;
        }

        /**
         * 命名空间删除
         *
         * @param dataEntity
         * @return
         */
        public ApiResponse namespacedelete(DataEntity dataEntity) {
            extendNameSpaceManager.setApplicationContext(applicationContext);

            List<String> namespaceList = NAMESPACE_SPLITTER.splitToList(dataEntity.getNamespace());
            Set<String> newNamespaceSet = new HashSet<>(namespaceList);

            Map<String, Map<String, String>> configMap = extendNameSpaceManager.getDeleteNamespaceConfig(newNamespaceSet);

            extendAdminWebfluxProcessor.process(applicationContext, configMap, "ApolloExtendAdminWebFlux.DefaultServiceHandler#namespacedelete");
            deletePostHandler(newNamespaceSet);
            return null;
        }

        private void injectPostHandler(Set<String> namespaceSet) {
            ConfigurableEnvironment environment = applicationContext.getEnvironment();
            SimpleCompositePropertySource compositePropertySource = ApolloExtendUtils.getCompositePropertySource(environment);

            if (compositePropertySource.contains(CommonApolloConstant.ADMIN_ENDPOINT_PROPERTY_SOURCES_NAME)) {
                MapPropertySource mapPropertySource = (MapPropertySource) compositePropertySource.get(CommonApolloConstant.ADMIN_ENDPOINT_PROPERTY_SOURCES_NAME);
                Map<String, Object> source = mapPropertySource.getSource();
                String str = (String) source.get(CommonApolloConstant.APOLLO_EXTEND_NAMESPACE);
                Set<String> oldNamespaceSet = Sets.newHashSet(NAMESPACE_SPLITTER.splitToList(str));
                oldNamespaceSet.addAll(namespaceSet);
                String collect = oldNamespaceSet.stream().collect(Collectors.joining(CommonApolloConstant.DEFAULT_SEPARATOR));
                source.put(CommonApolloConstant.APOLLO_EXTEND_NAMESPACE, collect);
            } else {
                String collect = namespaceSet.stream().collect(Collectors.joining(CommonApolloConstant.DEFAULT_SEPARATOR));
                Map<String, Object> map = Maps.newHashMap();
                map.put(CommonApolloConstant.APOLLO_EXTEND_NAMESPACE, collect);

                compositePropertySource.addPropertySource(new MapPropertySource(CommonApolloConstant.ADMIN_ENDPOINT_PROPERTY_SOURCES_NAME, map));
            }
        }

        private void deletePostHandler(Set<String> namespaceSet) {
            ConfigurableEnvironment environment = applicationContext.getEnvironment();
            SimpleCompositePropertySource compositePropertySource = ApolloExtendUtils.getCompositePropertySource(environment);
            List<ApolloClientExtendConfig> list = compositePropertySource.getPropertySources().stream()
                    .filter(propertySource -> propertySource instanceof SimplePropertySource)
                    .filter(propertySource -> propertySource.containsProperty(CommonApolloConstant.APOLLO_EXTEND_NAMESPACE))
                    .map(propertySource -> ((SimplePropertySource) propertySource))
                    .map(configSource -> (ApolloClientExtendConfig) configSource.getSource())
                    .collect(Collectors.toList());
            for (ApolloClientExtendConfig apolloClientExtendConfig : list) {
                String nameSpaceConfig = apolloClientExtendConfig.getProperty(CommonApolloConstant.APOLLO_EXTEND_NAMESPACE, CommonApolloConstant.NAMESPACE_APPLICATION);
                Set<String> oldNamespaceSet = Sets.newHashSet(NAMESPACE_SPLITTER.splitToList(nameSpaceConfig));
                Set<String> commonSet = Sets.intersection(oldNamespaceSet, namespaceSet);
                if (CollectionUtils.isNotEmpty(commonSet)) {
                    String collect = Sets.difference(oldNamespaceSet, commonSet).stream().collect(Collectors.joining(CommonApolloConstant.DEFAULT_SEPARATOR));
                    apolloClientExtendConfig.setProperty(CommonApolloConstant.APOLLO_EXTEND_NAMESPACE, collect);
                }
            }
            //  如果没有配置，则找 MapPropertySource
            if (CollectionUtils.isEmpty(list)) {
                List<Map<String, Object>> propertySourceList = compositePropertySource.getPropertySources().stream()
                        .filter(propertySource -> propertySource instanceof MapPropertySource)
                        .filter(p -> p.containsProperty(CommonApolloConstant.APOLLO_EXTEND_NAMESPACE))
                        .map(propertySource -> ((MapPropertySource) propertySource).getSource())
                        .collect(Collectors.toList())
                        ;
                if (CollectionUtils.isNotEmpty(propertySourceList)) {
                    for (Map<String, Object> propertySourceMap : propertySourceList) {
                        String nameSpaceConfig = (String) propertySourceMap.get(CommonApolloConstant.APOLLO_EXTEND_NAMESPACE);
                        Set<String> oldNamespaceSet = Sets.newHashSet(NAMESPACE_SPLITTER.splitToList(nameSpaceConfig));
                        Set<String> commonSet = Sets.intersection(oldNamespaceSet, namespaceSet);
                        if (CollectionUtils.isNotEmpty(commonSet)) {
                            String collect = Sets.difference(oldNamespaceSet, commonSet).stream().collect(Collectors.joining(CommonApolloConstant.DEFAULT_SEPARATOR));
                            propertySourceMap.put(CommonApolloConstant.APOLLO_EXTEND_NAMESPACE, collect);
                        }
                    }
                }
            }
        }
    }

}
