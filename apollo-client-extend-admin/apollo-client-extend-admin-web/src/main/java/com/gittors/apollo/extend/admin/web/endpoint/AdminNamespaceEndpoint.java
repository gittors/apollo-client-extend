package com.gittors.apollo.extend.admin.web.endpoint;

import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.gittors.apollo.extend.admin.web.spi.ApolloExtendAdminProcessor;
import com.gittors.apollo.extend.common.constant.ApolloExtendAdminConstant;
import com.gittors.apollo.extend.common.constant.CommonApolloConstant;
import com.gittors.apollo.extend.common.spi.ServiceLookUp;
import com.gittors.apollo.extend.spi.ApolloExtendNameSpaceManager;
import com.google.common.base.Splitter;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.ConfigurableEnvironment;
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
    private ConfigurableEnvironment environment;

    @Autowired
    private BeanFactory beanFactory;

    @RequestMapping(path = "/inject-namespace", method = RequestMethod.POST)
    @ApiOperationSupport(order = 1)
    @ApiOperation(value = "注入命名空间", notes = "", response = ResponseEntity.class, httpMethod = "POST")
    @ResponseBody
    public ResponseEntity<?> injectNamespace(@ApiParam(value = "token", required = true)
                                                 @RequestParam("token") String token,
                                             @ApiParam(value = "命名空间", required = true)
                                              @RequestParam("namespace") String namespace) {
        extendNameSpaceManager.setBeanFactory(beanFactory);
        extendNameSpaceManager.setEnvironment(environment);

        List<String> namespaceList = NAMESPACE_SPLITTER.splitToList(namespace);
        Set<String> newNamespaceSet = new HashSet<>(namespaceList);

        List<Map<String, String>> list = extendNameSpaceManager.getAddNamespace(newNamespaceSet);

        apolloExtendAdminProcessor.process(beanFactory, list);
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
        extendNameSpaceManager.setBeanFactory(beanFactory);
        extendNameSpaceManager.setEnvironment(environment);

        List<String> namespaceList = NAMESPACE_SPLITTER.splitToList(namespace);
        Set<String> newNamespaceSet = new HashSet<>(namespaceList);

        List<Map<String, String>> list = extendNameSpaceManager.getDeleteNamespace(newNamespaceSet);

        apolloExtendAdminProcessor.process(beanFactory, list);
        return ResponseEntity.ok(ApolloExtendAdminConstant.OK);
    }

}
