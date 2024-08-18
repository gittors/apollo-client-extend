package com.gittors.apollo.extend.properties;

import com.gittors.apollo.extend.common.enums.ChangeType;
import com.google.common.collect.Maps;
import lombok.Data;
import org.apache.commons.collections4.MapUtils;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 配置前缀：
 *  {@link com.gittors.apollo.extend.common.constant.CommonApolloConstant#APOLLO_EXTEND_GLOBAL_LISTEN_KEY_SUFFIX}
 *
 * @author zlliu
 * @date 2020/8/28 14:52
 */
@Data
public class ApolloExtendGlobalListenKeyProperties {
    /**
     * 监听Key配置
     * {Key: 命名空间名称， value: 监听Key集合}
     */
    private Map<String, Set<String>> map = Maps.newLinkedHashMap();

    /**
     * 合并配置
     * @param listenKeyProperties
     * @param changeType
     * @return
     */
    public Map<String, Set<String>> merge(ApolloExtendListenKeyProperties listenKeyProperties, ChangeType changeType) {
        Map<String, Set<String>> mergeMap = Maps.newLinkedHashMap(this.map);
        switch (changeType) {
            case ADD:
                if (MapUtils.isEmpty(mergeMap)) {
                    return Maps.newHashMap(listenKeyProperties.getAddMap());
                } else if (MapUtils.isEmpty(listenKeyProperties.getAddMap())) {
                    return mergeMap;
                } else {
                    listenKeyProperties.getAddMap()
                            .forEach((key, value) -> mergeMap.merge(key, value, (v1, v2) ->
                                    Stream.concat(v1.stream(), v2.stream()).collect(Collectors.toSet())));
                }
                break;
            case DELETE:
                if (MapUtils.isEmpty(mergeMap)) {
                    return Maps.newHashMap(listenKeyProperties.getDelMap());
                } else if (MapUtils.isEmpty(listenKeyProperties.getDelMap())) {
                    return mergeMap;
                } else {
                    listenKeyProperties.getDelMap()
                            .forEach((key, value) -> mergeMap.merge(key, value, (v1, v2) ->
                                    Stream.concat(v1.stream(), v2.stream()).collect(Collectors.toSet())));
                }
                break;
            default:
                break;
        }
        return mergeMap;
    }

}
