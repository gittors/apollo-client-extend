package com.gittors.apollo.extend.service;

import com.ctrip.framework.apollo.core.ConfigConsts;
import com.gittors.apollo.extend.callback.AbstractApolloExtendCallback;
import com.gittors.apollo.extend.common.constant.CommonApolloConstant;
import com.gittors.apollo.extend.common.service.ServiceLookUp;
import com.gittors.apollo.extend.spi.ApolloExtendNameSpaceManager;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Semaphore;

/**
 * @author zlliu
 * @date 2020/7/8 20:29
 */
@Slf4j
public class ApolloExtendCallbackAdapter extends AbstractApolloExtendCallback implements ApplicationContextAware {

    private final ApolloExtendNameSpaceManager extendNameSpaceManager =
            ServiceLookUp.loadPrimary(ApolloExtendNameSpaceManager.class);

    /**
     * 限制修改 {@link CommonApolloConstant.APOLLO_EXTEND_NAMESPACE} 配置的线程数
     */
    private Semaphore semaphore = new Semaphore(1);

    private ConfigurableApplicationContext applicationContext;

    public ApolloExtendCallbackAdapter() {
    }

    @Override
    public void callback(String oldValue, String newValue, Object... objects) {
        Semaphore semaphore = lockConfigure();
        if (semaphore == null || !semaphore.tryAcquire()) {
            log.warn("#callback multiple updates are not allowed for the same configuration!");
            return;
        }
        try {
            doCallback(oldValue, newValue);
        } finally {
            semaphore.release();
        }
    }

    protected void doCallback(String oldValue, String newValue) {
        //  管理命名空间
        String manageNamespaces = newValue;
        if (StringUtils.isNotBlank(manageNamespaces) ||
                StringUtils.isBlank(manageNamespaces) && (manageNamespaces = ConfigConsts.NAMESPACE_APPLICATION) != null) {
            List<String> namespaceList = NAMESPACE_SPLITTER.splitToList(manageNamespaces);
            Set<String> newNamespaceSet = new HashSet<>(namespaceList);
            newNamespaceSet.add(ConfigConsts.NAMESPACE_APPLICATION);
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
            this.changeProcess(data);
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
     * 配置有变更，客户端的处理
     * 备注：此时配置已经刷新至 Spring环境，此方法留待客户端回调后续处理
     *
     * @param data  变更的数据 {Key: 命名空间, Value: {配置Key=配置Value}}
     */
    protected void changeProcess(Map<String, Map<String, String>> data) {
    }

    @Override
    public String listenKey() {
        return getConfigPrefix();
    }

    @Override
    protected String getConfigPrefix() {
        String extNamespaceConfig = applicationContext.getEnvironment().getProperty(CommonApolloConstant.APOLLO_EXTEND_NAMESPACE_PREFIX, "");
        if (StringUtils.isNotBlank(extNamespaceConfig)) {
            return extNamespaceConfig;
        } else {
            return CommonApolloConstant.APOLLO_EXTEND_NAMESPACE;
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = (ConfigurableApplicationContext) applicationContext;
    }
}
