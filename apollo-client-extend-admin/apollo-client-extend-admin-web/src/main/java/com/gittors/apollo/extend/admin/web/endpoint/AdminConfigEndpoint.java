package com.gittors.apollo.extend.admin.web.endpoint;

import com.gittors.apollo.extend.common.constant.ApolloExtendAdminConstant;
import com.gittors.apollo.extend.common.context.ApolloPropertySourceContext;
import com.gittors.apollo.extend.common.env.SimplePropertySource;
import com.gittors.apollo.extend.common.event.BinderRefreshBinderEvent;
import com.gittors.apollo.extend.event.EventPublisher;
import com.gittors.apollo.extend.support.ext.ApolloClientExtendConfig;
import com.gittors.apollo.extend.utils.ApolloExtendUtils;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import java.util.Map;

/**
 * @author zlliu
 * @date 2020/8/24 16:54
 */
@RestController
@RequestMapping(path = "/config")
@Api(tags = { "配置接口" })
@ApiIgnore
@Deprecated
public class AdminConfigEndpoint {

    @Autowired
    private BeanFactory beanFactory;

    @RequestMapping(path = "/update-config", method = RequestMethod.POST)
    @ApiOperation(value = "更新配置", notes = "", response = ResponseEntity.class, httpMethod = "POST")
    @ResponseBody
    @Deprecated
    public ResponseEntity<?> updateConfig(@ApiParam(value = "token", required = true)
                                              @RequestParam("token") String token,
                                          @ApiParam(value = "命名空间", required = true)
                                              @RequestParam("namespace") String namespace,
                                              @ApiParam(value = "配置key", required = true)
                                              @RequestParam("configKey") String configKey,
                                          @ApiParam(value = "配置value", required = true)
                                          @RequestParam("configValue") String configValue) {
        SimplePropertySource simplePropertySource = ApolloExtendUtils.findPropertySource(ApolloPropertySourceContext.INSTANCE.getPropertySources(),
                (propertySource) -> propertySource.getNamespace().equals(namespace));
        if (simplePropertySource == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Namespace Didn't Find!");
        }
        return update(simplePropertySource, configKey, configValue);
    }

    private ResponseEntity<?> update(SimplePropertySource propertySource, String key, String value) {
        ApolloClientExtendConfig config = (ApolloClientExtendConfig) propertySource.getSource();
        config.setProperty(key, value);

        //  发送绑定事件
        Map<String, Map<String, String>> data = Maps.newHashMap();
        data.put(propertySource.getNamespace(), ImmutableMap.of(key, value));
        pushBinder(data);

        return ResponseEntity.ok().body(ApolloExtendAdminConstant.OK);
    }

    /**
     * 发送binder事件
     * @param data
     */
    public void pushBinder(Map<String, Map<String, String>> data) {
        BinderRefreshBinderEvent binderRefreshBinderEvent = BinderRefreshBinderEvent.getInstance();
        binderRefreshBinderEvent.setData(data);
        binderRefreshBinderEvent.setSource("AdminConfigEndpoint#update");
        EventPublisher eventPublisher = beanFactory.getBean(EventPublisher.class);
        eventPublisher.asyncPublish(binderRefreshBinderEvent);
    }

}
