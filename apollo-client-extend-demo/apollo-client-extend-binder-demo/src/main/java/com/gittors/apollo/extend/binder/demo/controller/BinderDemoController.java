package com.gittors.apollo.extend.binder.demo.controller;

import com.gittors.apollo.extend.binder.demo.properties.MyProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author zlliu
 * @date 2020/7/22 20:58
 */
@Slf4j
@RestController
@EnableConfigurationProperties({MyProperties.class})
public class BinderDemoController {

    @Autowired
    private MyProperties myProperties;

    @Autowired
    private ConfigurableEnvironment environment;

    @GetMapping("/get")
    public MyProperties get() {
        return myProperties;
    }

}
