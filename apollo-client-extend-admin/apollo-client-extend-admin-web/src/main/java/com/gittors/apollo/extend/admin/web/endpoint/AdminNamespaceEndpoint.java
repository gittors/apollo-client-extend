package com.gittors.apollo.extend.admin.web.endpoint;

import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.gittors.apollo.extend.admin.web.spi.ApolloExtendAdminProcessor;
import com.gittors.apollo.extend.common.constant.ApolloExtendAdminConstant;
import com.gittors.apollo.extend.common.constant.CommonApolloConstant;
import com.gittors.apollo.extend.common.env.SimplePropertySource;
import com.gittors.apollo.extend.common.service.ServiceLookUp;
import com.gittors.apollo.extend.env.SimpleCompositePropertySource;
import com.gittors.apollo.extend.spi.ApolloExtendNameSpaceManager;
import com.gittors.apollo.extend.support.ext.ApolloClientExtendConfig;
import com.gittors.apollo.extend.utils.ApolloExtendUtils;
import com.google.common.base.Splitter;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author zlliu
 * @date 2020/8/24 16:54
 */
@RestController
@RequestMapping(path = "/namespace")
@Api(tags = { "命名空间接口" })
public class AdminNamespaceEndpoint {
    private final ApolloExtendNameSpaceManager extendNameSpaceManager =
            ServiceLookUp.loadPrimary(ApolloExtendNameSpaceManager.class);

    private static final Splitter NAMESPACE_SPLITTER =
            Splitter.on(CommonApolloConstant.DEFAULT_SEPARATOR).omitEmptyStrings().trimResults();

    private final ApolloExtendAdminProcessor<BeanFactory> apolloExtendAdminProcessor =
            ServiceLookUp.loadPrimary(ApolloExtendAdminProcessor.class);

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @RequestMapping(path = "/inject-namespace", method = RequestMethod.POST)
    @ApiOperationSupport(order = 1)
    @ApiOperation(value = "注入命名空间", notes = "", response = ResponseEntity.class, httpMethod = "POST")
    @ResponseBody
    public ResponseEntity<?> injectNamespace(@ApiParam(value = "token", required = true)
                                                 @RequestParam("token") String token,
                                             @ApiParam(value = "命名空间", required = true)
                                              @RequestParam("namespace") String namespace) {
        extendNameSpaceManager.setApplicationContext(applicationContext);

        List<String> namespaceList = NAMESPACE_SPLITTER.splitToList(namespace);
        Set<String> newNamespaceSet = new HashSet<>(namespaceList);

        Map<String, Map<String, String>> configMap = extendNameSpaceManager.getAddNamespaceConfig(newNamespaceSet);

        apolloExtendAdminProcessor.process(applicationContext, configMap, "AdminNamespaceEndpoint#injectNamespace");
        //  添加命名空间，需要注入到配置
        injectPostHandler(newNamespaceSet);
        return ResponseEntity.ok(ApolloExtendAdminConstant.OK);
    }

    @RequestMapping(path = "/delete-namespace", method = RequestMethod.POST)
    @ApiOperationSupport(order = 2)
    @ApiOperation(value = "删除命名空间", notes = "", response = ResponseEntity.class, httpMethod = "POST")
    @ResponseBody
    public ResponseEntity<?> deleteNamespace(@ApiParam(value = "token", required = true)
                                                 @RequestParam("token") String token,
                                             @ApiParam(value = "命名空间", required = true)
                                              @RequestParam("namespace") String namespace) {
        extendNameSpaceManager.setApplicationContext(applicationContext);

        List<String> namespaceList = NAMESPACE_SPLITTER.splitToList(namespace);
        Set<String> namespaceSet = new HashSet<>(namespaceList);

        Map<String, Map<String, String>> configMap = extendNameSpaceManager.getDeleteNamespaceConfig(namespaceSet);

        apolloExtendAdminProcessor.process(applicationContext, configMap, "AdminNamespaceEndpoint#deleteNamespace");
        //  删除命名空间，则Spring环境里面的配置也应该删除，否则listen.key.delMap的监听KEY处理逻辑会被拦截
        deletePostHandler(namespaceSet);
        return ResponseEntity.ok(ApolloExtendAdminConstant.OK);
    }

    private void injectPostHandler(Set<String> namespaceSet) {
        ConfigurableEnvironment environment = (ConfigurableEnvironment) applicationContext.getEnvironment();
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
        ConfigurableEnvironment environment = (ConfigurableEnvironment) applicationContext.getEnvironment();
        SimpleCompositePropertySource compositePropertySource = ApolloExtendUtils.getCompositePropertySource(environment);
        List<ApolloClientExtendConfig> list = compositePropertySource.getPropertySources().stream()
                .filter(propertySource -> propertySource instanceof SimplePropertySource)
                .filter(propertySource -> propertySource.containsProperty(CommonApolloConstant.APOLLO_EXTEND_NAMESPACE))
                .map(propertySource -> ((SimplePropertySource) propertySource))
                .map(config -> (ApolloClientExtendConfig) config)
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
