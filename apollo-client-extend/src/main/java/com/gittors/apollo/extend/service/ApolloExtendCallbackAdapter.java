package com.gittors.apollo.extend.service;

import com.ctrip.framework.apollo.core.ConfigConsts;
import com.gittors.apollo.extend.callback.AbstractApolloExtendCallback;
import com.gittors.apollo.extend.common.constant.CommonApolloConstant;
import com.gittors.apollo.extend.common.enums.ChangeType;
import com.gittors.apollo.extend.common.spi.ServiceLookUp;
import com.gittors.apollo.extend.spi.ApolloExtendNameSpaceManager;
import com.google.common.base.Splitter;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.ConfigurableEnvironment;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author zlliu
 * @date 2020/7/8 20:29
 */
@Slf4j
public abstract class ApolloExtendCallbackAdapter extends AbstractApolloExtendCallback {

    private static final Splitter NAMESPACE_SPLITTER =
            Splitter.on(CommonApolloConstant.DEFAULT_SEPARATOR).omitEmptyStrings().trimResults();

    private final ApolloExtendNameSpaceManager extendNameSpaceManager =
            ServiceLookUp.loadPrimary(ApolloExtendNameSpaceManager.class);

    @Autowired
    private ConfigurableEnvironment environment;

    @Autowired
    private BeanFactory beanFactory;

    public ApolloExtendCallbackAdapter() {
    }

    @Override
    public void callback(String namespace, String oldValue, String newValue, long timestamp) {
        doCallback(oldValue, newValue);
    }

    protected void doCallback(String oldValue, String newValue) {
        //  管理命名空间
        String manageNamespaces = newValue;
        if (StringUtils.isNotBlank(manageNamespaces) ||
                StringUtils.isBlank(manageNamespaces) && (manageNamespaces = ConfigConsts.NAMESPACE_APPLICATION) != null) {
            List<String> namespaceList = NAMESPACE_SPLITTER.splitToList(manageNamespaces);
            Set<String> newNamespaceSet = new HashSet<>(namespaceList);
            newNamespaceSet.add(ConfigConsts.NAMESPACE_APPLICATION);

            extendNameSpaceManager.setBeanFactory(beanFactory);
            extendNameSpaceManager.setEnvironment(environment);

            //  如有新增配置项，刷新环境
            Map<String, Map<String, String>> addPropertySourceList = extendNameSpaceManager.getAddNamespace(excludeNamespace(newNamespaceSet));

            //  如有删除配置项，刷新环境
            Map<String, Map<String, String>> deletePropertySourceList = extendNameSpaceManager.getDeleteNamespace(getDifferentNamespace(oldValue, newNamespaceSet));
            //  {Key: 新增/删除, Value: {Key: 命名空间, Value: 配置Key=配置Value}}
            Map<String, Map<String, Map<String, String>>> data = Maps.newHashMap();
            if (MapUtils.isNotEmpty(addPropertySourceList) && MapUtils.isNotEmpty(deletePropertySourceList)) {
                data.put(ChangeType.ADD.name(), addPropertySourceList);
                data.put(ChangeType.DELETE.name(), deletePropertySourceList);

                changeProcess(ChangeType.BOTH, data);
            } else if (MapUtils.isNotEmpty(addPropertySourceList) && MapUtils.isEmpty(deletePropertySourceList)) {
                data.put(ChangeType.ADD.name(), addPropertySourceList);

                changeProcess(ChangeType.ADD, data);
            } else if (MapUtils.isEmpty(addPropertySourceList) && MapUtils.isNotEmpty(deletePropertySourceList)) {
                data.put(ChangeType.DELETE.name(), deletePropertySourceList);

                changeProcess(ChangeType.DELETE, data);
            }
        }
    }

    private Set<String> excludeNamespace(Set<String> namespaceSet) {
        Set<String> exclude = namespaceSet.stream()
                .filter(namespace -> !ConfigConsts.NAMESPACE_APPLICATION.equalsIgnoreCase(namespace))
                .collect(Collectors.toSet());
        return exclude;
    }

    /**
     * 计算需要删除的命名空间
     * @param oldValue
     * @param newNamespaceSet
     * @return
     */
    private Set<String> getDifferentNamespace(String oldValue, Set<String> newNamespaceSet) {
        List<String> oldNamespaceList = NAMESPACE_SPLITTER.splitToList(StringUtils.isNotBlank(oldValue) ? oldValue : ConfigConsts.NAMESPACE_APPLICATION);
        Set<String> oldNamespaceSet = new HashSet<>(oldNamespaceList);
        oldNamespaceSet.add(ConfigConsts.NAMESPACE_APPLICATION);

        Set<String> commonSet = Sets.intersection(oldNamespaceSet, newNamespaceSet);
        return excludeNamespace(Sets.difference(oldNamespaceSet, commonSet));
    }

    /**
     * 配置有变更，客户端的处理
     * @param changeType    变更类型
     * @param data  变更的数据 {Key: 新增/删除, Value: {Key: 命名空间, Value: 配置Key=配置Value}}
     */
    protected abstract void changeProcess(ChangeType changeType, Map<String, Map<String, Map<String, String>>> data);

    @Override
    public List<String> keyList() {
        return Collections.singletonList(getConfigPrefix());
    }

    @Override
    protected String getConfigPrefix() {
        String extNamespaceConfig = environment.getProperty(CommonApolloConstant.APOLLO_EXTEND_NAMESPACE_PREFIX);
        if (StringUtils.isNotBlank(extNamespaceConfig)) {
            return extNamespaceConfig;
        } else {
            return CommonApolloConstant.APOLLO_EXTEND_NAMESPACE;
        }
    }

}
