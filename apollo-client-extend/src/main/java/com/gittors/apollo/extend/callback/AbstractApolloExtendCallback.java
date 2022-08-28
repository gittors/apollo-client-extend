package com.gittors.apollo.extend.callback;

import com.gittors.apollo.extend.common.constant.CommonApolloConstant;
import com.google.common.base.Splitter;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author zlliu
 * @date 2020/7/8 11:29
 */
public abstract class AbstractApolloExtendCallback implements ApolloExtendCallback {
    //  分割器
    public static final Splitter NAMESPACE_SPLITTER =
            Splitter.on(CommonApolloConstant.DEFAULT_SEPARATOR).omitEmptyStrings().trimResults();

    @Override
    public String listenKey() {
        return getConfigPrefix() + ".*";
    }

    /**
     * 监听的配置前缀
     * @return
     */
    protected String getConfigPrefix() {
        throw new RuntimeException("configPrefix not supported");
    }

    /**
     * 计算需要删除的命名空间
     * @param oldValue
     * @param newNamespaceSet
     * @return
     */
    protected Set<String> getDifferentNamespace(String oldValue, Set<String> newNamespaceSet) {
        List<String> oldNamespaceList = NAMESPACE_SPLITTER.splitToList(StringUtils.isNotBlank(oldValue) ? oldValue : CommonApolloConstant.NAMESPACE_APPLICATION);
        Set<String> oldNamespaceSet = new HashSet<>(oldNamespaceList);
        oldNamespaceSet.add(CommonApolloConstant.NAMESPACE_APPLICATION);

        Set<String> commonSet = Sets.intersection(oldNamespaceSet, newNamespaceSet);
        return excludeNamespace(Sets.difference(oldNamespaceSet, commonSet));
    }

    /**
     * 排除application 命名空间
     * @param namespaceSet
     * @return
     */
    protected Set<String> excludeNamespace(Set<String> namespaceSet) {
        Set<String> exclude = namespaceSet.stream()
                .filter(namespace -> !CommonApolloConstant.NAMESPACE_APPLICATION.equalsIgnoreCase(namespace))
                .collect(Collectors.toSet());
        return exclude;
    }
}
