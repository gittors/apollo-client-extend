package com.gittors.apollo.extend.service;

import com.ctrip.framework.apollo.model.ConfigChange;
import com.ctrip.framework.apollo.model.ConfigChangeEvent;
import com.gittors.apollo.extend.common.constant.CommonApolloConstant;
import com.gittors.apollo.extend.common.service.CommonServiceLoader;
import com.gittors.apollo.extend.spi.ApolloExtendConfigPostProcessor;
import com.gittors.apollo.extend.spi.ApolloExtendNameSpaceManager;
import com.google.common.base.Splitter;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.annotation.Order;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;

/**
 * @author zlliu
 * @date 2020/7/8 20:29
 */
@Slf4j
@Order(1)
public class ApolloExtendCallbackAdapter implements ApplicationContextAware, SimpleConfigListenerHandler {

    //  分割器
    private static final Splitter NAMESPACE_SPLITTER =
            Splitter.on(CommonApolloConstant.DEFAULT_SEPARATOR).omitEmptyStrings().trimResults();

    private final ApolloExtendNameSpaceManager extendNameSpaceManager =
            CommonServiceLoader.loadPrimary(ApolloExtendNameSpaceManager.class);

    private final ApolloExtendConfigPostProcessor configPostProcessor =
            CommonServiceLoader.loadPrimary(ApolloExtendConfigPostProcessor.class);

    /**
     * 限制修改 {@link CommonApolloConstant#APOLLO_EXTEND_NAMESPACE} 配置的线程数
     */
    private Semaphore semaphore = new Semaphore(1);

    private ConfigurableApplicationContext applicationContext;

    @Override
    public boolean match(String key) {
        return StringUtils.equalsIgnoreCase(key, getConfigKey());
    }

    @Override
    public void handle(ConfigChangeEvent changeEvent) {
        Semaphore semaphore = lockConfigure();
        if (semaphore == null || !semaphore.tryAcquire()) {
            log.warn("#callback multiple updates are not allowed for the same configuration!");
            return;
        }
        try {
            for (String changedKey : changeEvent.changedKeys()) {
                if (StringUtils.equalsIgnoreCase(changedKey, getConfigKey())) {
                    ConfigChange configChange = changeEvent.getChange(changedKey);
                    doCallback(configChange.getOldValue(), configChange.getNewValue());
                }
            }
        } finally {
            semaphore.release();
        }
    }

    protected void doCallback(String oldValue, String newValue) {
        //  管理命名空间
        String manageNamespaces = newValue;
        if (StringUtils.isNotBlank(manageNamespaces) ||
                StringUtils.isBlank(manageNamespaces) && (manageNamespaces = CommonApolloConstant.NAMESPACE_APPLICATION) != null) {
            List<String> namespaceList = NAMESPACE_SPLITTER.splitToList(manageNamespaces);
            Set<String> newNamespaceSet = new HashSet<>(namespaceList);
            newNamespaceSet.add(CommonApolloConstant.NAMESPACE_APPLICATION);
            //  设置环境
            extendNameSpaceManager.setApplicationContext(applicationContext);

            //  如有新增配置项，刷新环境
            Map<String, Map<String, String>> addPropertySourceList =
                    extendNameSpaceManager.getAddNamespaceConfig(excludeNamespace(newNamespaceSet));

            //  如有删除配置项，刷新环境
            Map<String, Map<String, String>> deletePropertySourceList =
                    extendNameSpaceManager.getDeleteNamespaceConfig(getDifferentNamespace(oldValue, newNamespaceSet));
            //  {Key: 命名空间, Value: {配置Key=配置Value}}
            Map<String, Map<String, String>> data = Maps.newHashMap();
            data.putAll(addPropertySourceList);
            data.putAll(deletePropertySourceList);
            //  配置更新回调
            configPostProcessor.postProcess(applicationContext, data);
        }
    }

    private String getConfigKey() {
        String property = applicationContext.getEnvironment().getProperty(CommonApolloConstant.APOLLO_EXTEND_NAMESPACE_PREFIX, "");
        if (StringUtils.isNotBlank(property)) {
            return property;
        } else {
            return CommonApolloConstant.APOLLO_EXTEND_NAMESPACE;
        }
    }

    /**
     * 并发限制
     * @return
     */
    protected Semaphore lockConfigure() {
        return this.semaphore;
    }

    /**
     * 计算需要删除的命名空间
     * @param oldValue
     * @param newNamespaceSet
     * @return
     */
    private Set<String> getDifferentNamespace(String oldValue, Set<String> newNamespaceSet) {
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
    private Set<String> excludeNamespace(Set<String> namespaceSet) {
        Set<String> exclude = namespaceSet.stream()
                .filter(namespace -> !CommonApolloConstant.NAMESPACE_APPLICATION.equalsIgnoreCase(namespace))
                .collect(Collectors.toSet());
        return exclude;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = (ConfigurableApplicationContext) applicationContext;
    }
}
