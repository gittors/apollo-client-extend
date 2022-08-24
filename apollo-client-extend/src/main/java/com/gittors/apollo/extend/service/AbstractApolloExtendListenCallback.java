package com.gittors.apollo.extend.service;

import com.ctrip.framework.apollo.enums.ConfigSourceType;
import com.ctrip.framework.apollo.model.ConfigChangeEvent;
import com.ctrip.framework.apollo.spring.config.ConfigPropertySource;
import com.ctrip.framework.apollo.spring.config.ConfigPropertySourceFactory;
import com.ctrip.framework.apollo.spring.util.SpringInjector;
import com.gittors.apollo.extend.callback.AbstractApolloExtendCallback;
import com.gittors.apollo.extend.common.constant.CommonApolloConstant;
import com.gittors.apollo.extend.common.enums.ChangeType;
import com.gittors.apollo.extend.common.service.ServiceLookUp;
import com.gittors.apollo.extend.spi.ApolloExtendListenPublish;
import com.gittors.apollo.extend.support.ext.ApolloClientExtendConfig;
import com.gittors.apollo.extend.utils.ApolloExtendUtils;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.boot.context.properties.source.ConfigurationPropertySources;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;

/**
 * @author zlliu
 * @date 2021/5/8 22:27
 */
@Slf4j
public abstract class AbstractApolloExtendListenCallback extends AbstractApolloExtendCallback {
    private final ConfigPropertySourceFactory configPropertySourceFactory =
            SpringInjector.getInstance(ConfigPropertySourceFactory.class);

    private ConfigurableEnvironment environment;
    private BeanFactory beanFactory;

    public AbstractApolloExtendListenCallback(ApplicationContext applicationContext) {
        this.environment = (ConfigurableEnvironment) applicationContext.getEnvironment();
        this.beanFactory = applicationContext.getAutowireCapableBeanFactory();
    }

    @Override
    public void callback(String oldValue, String newValue, Object... objects) {
        Semaphore semaphore = lockConfigure();
        //  更新操作并发控制
        if (semaphore == null || !semaphore.tryAcquire()) {
            log.warn("#callback multiple updates are not allowed for the same configuration!");
            return;
        }
        try {
            ConfigChangeEvent changeEvent = (ConfigChangeEvent) objects[0];
            //  当前更新的key, 如： listen.key.addMap.application-test
            String key = changeEvent.changedKeys().stream().findFirst().get();
            //  当前更新操作的命名空间
            String namespace = changeEvent.getNamespace();
            //  截取当前更新key的命名空间,如： listen.key.addMap.application-test ==> application-test
            String managerNamespace = key.substring(key.lastIndexOf(".") + 1);
            if (StringUtils.isBlank(managerNamespace)) {
                log.warn("#callback key:{},managerNamespace is empty!", key);
                return;
            }

            //  1、获得 管理配置：apollo.extend.namespace 的配置值校验
            //  管理配置应该包含两部分：自身命名空间的 + application命名空间的
            String managerNamespaceConfig = getManagerConfig();
            if (check(managerNamespaceConfig, managerNamespace)) {
                return;
            }
            //  2、如果1的命名空间已存在，且修改了：listen.key.addMap 或delMap 配置的值，直接根据配置刷新Spring环境
            Set<String> newNamespaceSet = Sets.newHashSet(managerNamespace);
            ChangeType changeType = judgmentChangeType(managerNamespaceConfig, managerNamespace);
            if (changeType == null) {
                log.warn("#callback changeType is null");
                return;
            }
            Map<String, Map.Entry<Boolean, Set<String>>> managerConfigMap = ApolloExtendUtils.getManagerConfig(environment, newNamespaceSet, changeType);

            //  获取当前更新key对应的命名空间
            //  获得propertySource, 相同的取最后一个
            Optional<ConfigPropertySource> optional = configPropertySourceFactory.getAllConfigPropertySources()
                    .stream()
                    .filter(configPropertySource -> configPropertySource.getSource().getSourceType() != ConfigSourceType.NONE)
                    .filter(configPropertySource -> managerNamespace.equals(configPropertySource.getName()))
                    .reduce((first, second) -> second)
                    ;
            if (!optional.isPresent()) {
                log.warn("#callback configPropertySource can't be find");
                return;
            }
            ConfigPropertySource configPropertySource = optional.get();
            ApolloClientExtendConfig defaultConfig = (ApolloClientExtendConfig) configPropertySource.getSource();
            Properties properties = new Properties();
            //  前置处理
            propertiesBeforeHandler(properties, defaultConfig, managerConfigMap.get(configPropertySource.getName()), changeType);
            //  刷新对象
            defaultConfig.updateConfig(properties, configPropertySource.getSource().getSourceType());
            //  后置处理-设置回调
            propertiesAfterHandler(defaultConfig, managerConfigMap.get(configPropertySource.getName()), changeType);
            //  发布更新事件
            publish(properties);
        } finally {
            semaphore.release();
        }
    }

    private void publish(Properties properties) {
        ApolloExtendListenPublish publish = ServiceLookUp.loadPrimary(ApolloExtendListenPublish.class);
        publish.doPublish(beanFactory, properties);
    }

    private String getManagerConfig() {
        MutablePropertySources mutablePropertySources = environment.getPropertySources();
        //  获得所有命名空间包含 "apollo.extend.namespace" 配置的值(用","号分隔)
        String namespaceConfig = mutablePropertySources.stream()
                .filter(pro -> !ConfigurationPropertySources.isAttachedConfigurationPropertySource(pro))
                .filter(pro -> pro.containsProperty(CommonApolloConstant.APOLLO_EXTEND_NAMESPACE))
                .map(pro -> (String) pro.getProperty(CommonApolloConstant.APOLLO_EXTEND_NAMESPACE))
                .distinct().collect(Collectors.joining(CommonApolloConstant.DEFAULT_SEPARATOR));
        List<String> managerConfigList = NAMESPACE_SPLITTER.splitToList(namespaceConfig);
        if (CollectionUtils.isEmpty(managerConfigList)) {
            return StringUtils.EMPTY;
        }
        //  去重 + 拼接
        String managerNamespaceConfig = managerConfigList.stream().distinct()
                .filter(config -> !"null".equals(config))
                .collect(Collectors.joining(CommonApolloConstant.DEFAULT_SEPARATOR));
        return managerNamespaceConfig;
    }

    /**
     * 并发限制
     * @return
     */
    protected Semaphore lockConfigure() {
        throw new RuntimeException("lockConfigure not supported");
    }

    /**
     * 校验命名空间：
     * 1、新增时，必须存在"listen.key"对应命名空间
     * 2、删除时，必须不存在"listen.key"对应命名空间
     *
     * @param managerNamespaceConfig    {@link CommonApolloConstant.APOLLO_EXTEND_NAMESPACE} 配置的命名空间
     * @param managerNamespace  {@link CommonApolloConstant.APOLLO_EXTEND_ADD_CALLBACK_CONFIG} 配置的命名空间
     * @return
     */
    protected boolean check(String managerNamespaceConfig, String managerNamespace) {
        return false;
    }

    /**
     * 判断配置修改类型
     * @return
     */
    protected ChangeType judgmentChangeType(String managerNamespaceConfig, String managerNamespace) {
        throw new RuntimeException("changeType not supported");
    }

    /**
     * 配置前置处理：新增或删除配置
     * @param sourceProperties
     * @param defaultConfig
     * @param configEntry
     */
    protected void propertiesBeforeHandler(final Properties sourceProperties, final ApolloClientExtendConfig defaultConfig,
                                           final Map.Entry<Boolean, Set<String>> configEntry, final ChangeType changeType) {
        throw new RuntimeException("propertiesPostHandler not supported");
    }

    /**
     * 配置后置处理：添加配置回调等
     * @param defaultConfig
     * @param configEntry
     */
    protected void propertiesAfterHandler(final ApolloClientExtendConfig defaultConfig, final Map.Entry<Boolean, Set<String>> configEntry, final ChangeType changeType) {
    }
}
