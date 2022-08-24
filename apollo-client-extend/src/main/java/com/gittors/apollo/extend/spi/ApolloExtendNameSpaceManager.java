package com.gittors.apollo.extend.spi;

import com.gittors.apollo.extend.common.service.Ordered;
import org.springframework.context.ApplicationContext;

import java.util.Map;
import java.util.Set;

/**
 * @author zlliu
 * @date 2020/8/26 10:33
 */
public interface ApolloExtendNameSpaceManager extends Ordered {

    /**
     * 设置环境
     * @param applicationContext
     */
    void setApplicationContext(ApplicationContext applicationContext);

    /**
     * 得到新增命名空间配置：{key: 命名空间名称, value: {key: 配置key, value: 配置key的值}}
     * @param needAddNamespaceSet
     * @return
     */
    Map<String, Map<String, String>> getAddNamespaceConfig(Set<String> needAddNamespaceSet);

    /**
     * 得到需删除的命名空间配置：{key: 命名空间名称, value: {key: 配置key, value: 配置key的值}}
     * @param needDeleteNamespaceSet
     * @return
     */
    Map<String, Map<String, String>> getDeleteNamespaceConfig(Set<String> needDeleteNamespaceSet);

}
