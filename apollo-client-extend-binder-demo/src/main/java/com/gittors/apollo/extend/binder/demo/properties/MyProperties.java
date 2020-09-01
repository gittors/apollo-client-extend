package com.gittors.apollo.extend.binder.demo.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;
import java.util.Set;

/**
 * @author zlliu
 * @date 2020/7/22 20:58
 */
@ConfigurationProperties(prefix = "my")
@Data
public class MyProperties {

    private Map<String, Set<String>> map;
}
