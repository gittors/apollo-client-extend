package com.gittors.apollo.extend.admin.web.endpoint;

import com.ctrip.framework.apollo.spring.config.ConfigPropertySource;
import com.ctrip.framework.apollo.spring.config.ConfigPropertySourceFactory;
import com.ctrip.framework.apollo.spring.util.SpringInjector;
import com.gittors.apollo.extend.binder.event.BinderRefreshBinderEvent;
import com.gittors.apollo.extend.common.constant.ApolloExtendAdminConstant;
import com.gittors.apollo.extend.event.EventPublisher;
import com.gittors.apollo.extend.support.ext.DefaultConfigExt;
import com.google.common.collect.Lists;
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

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

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

    private final ConfigPropertySourceFactory configPropertySourceFactory = SpringInjector
            .getInstance(ConfigPropertySourceFactory.class);

    @Autowired
    private BeanFactory beanFactory;

    @RequestMapping(path = "/update-config", method = RequestMethod.POST)
    @ApiOperation(value = "更新配置", notes = "", response = ResponseEntity.class, httpMethod = "POST")
    @ResponseBody
    public ResponseEntity<?> updateConfig(@ApiParam(value = "token", required = true)
                                              @RequestParam("token") String token,
                                          @ApiParam(value = "命名空间", required = true)
                                              @RequestParam("namespace") String namespace,
                                              @ApiParam(value = "配置key", required = true)
                                              @RequestParam("configKey") String configKey,
                                          @ApiParam(value = "配置value", required = true)
                                          @RequestParam("configValue") String configValue) {
        Optional<ConfigPropertySource> search =
                configPropertySourceFactory.getAllConfigPropertySources()
                        .stream()
                        .filter(cps -> cps.getName().equalsIgnoreCase(namespace))
                        .findFirst();
        if (!search.isPresent()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Namespace Didn't Find!");
        }
        return update(search.get(), configKey, configValue);
    }

    private ResponseEntity<?> update(ConfigPropertySource cps, String key, String value) {
        DefaultConfigExt configExt = (DefaultConfigExt) cps.getSource();
        configExt.setProperty(key, value);

        //  发送绑定事件
        Properties properties = new Properties();
        properties.setProperty(key, value);
        pushBinder(Lists.newArrayList(properties));

        return ResponseEntity.ok().body(ApolloExtendAdminConstant.OK);
    }

    /**
     * 发送binder事件
     * @param propertiesList
     */
    public void pushBinder(List<Properties> propertiesList) {
        Map<String, Map<String, String>> data = Maps.newHashMap();
        for (Properties properties : propertiesList) {
            data.put("namespace", Maps.fromProperties(properties));
        }

        EventPublisher eventPublisher = beanFactory.getBean(EventPublisher.class);
        eventPublisher.asyncPublish(new BinderRefreshBinderEvent(data));
    }

}
