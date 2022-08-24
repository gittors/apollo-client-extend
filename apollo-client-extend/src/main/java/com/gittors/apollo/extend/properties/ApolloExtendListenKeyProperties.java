package com.gittors.apollo.extend.properties;

import com.google.common.collect.Maps;
import lombok.Data;

import java.util.Map;
import java.util.Set;

/**
 * 配置前缀：
 *      {@link com.gittors.apollo.extend.common.constant.CommonApolloConstant#APOLLO_EXTEND_LISTEN_KEY_SUFFIX}
 *
 * @author zlliu
 * @date 2020/8/23 20:47
 */
@Data
public class ApolloExtendListenKeyProperties {

    /**
     * 配置命名空间的哪些配置生效
     */
    private Map<String, Set<String>> addMap = Maps.newLinkedHashMap();

    /**
     * 配置命名空间的哪些配置失效
     */
    private Map<String, Set<String>> delMap = Maps.newLinkedHashMap();
}
