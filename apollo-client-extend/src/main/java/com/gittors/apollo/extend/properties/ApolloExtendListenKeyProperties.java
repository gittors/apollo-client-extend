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
     * 监听Key配置：ADD 操作: {Key: 命名空间名称， value: 监听Key集合}
     * 备注：此配置的作用是，当 {@link com.gittors.apollo.extend.common.constant.CommonApolloConstant#APOLLO_EXTEND_NAMESPACE}
     * 的管理配置“新增”时，所管理命名空间的配置生效
     */
    private Map<String, Set<String>> addMap = Maps.newLinkedHashMap();

    /**
     * 监听Key配置：DELETE 操作：{key: 命名空间名称， value: 监听Key集合}
     * 备注：此配置的作用是，当 {@link com.gittors.apollo.extend.common.constant.CommonApolloConstant#APOLLO_EXTEND_NAMESPACE}
     * 的管理配置“删除”时，所管理命名空间的配置生效
     */
    private Map<String, Set<String>> delMap = Maps.newLinkedHashMap();
}
