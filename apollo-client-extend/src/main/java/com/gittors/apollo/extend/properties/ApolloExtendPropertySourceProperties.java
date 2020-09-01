package com.gittors.apollo.extend.properties;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

/**
 * 配置前缀：
 *      {@link com.gittors.apollo.extend.common.constant.CommonApolloConstant#PROPERTY_SOURCE_CONFIG_DEFAULT_SUFFIX}
 *
 * @author zlliu
 * @date 2020/8/24 10:26
 */
@Setter
@Getter
public class ApolloExtendPropertySourceProperties {
    /**
     * propertySource后缀配置
     */
    private Map<String, String> propertyMap = new HashMap<>();
}
