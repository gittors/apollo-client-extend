package com.gittors.apollo.extend.service;

import com.gittors.apollo.extend.common.constant.CommonApolloConstant;
import com.gittors.apollo.extend.common.enums.ChangeType;
import com.gittors.apollo.extend.support.ApolloExtendFactory;
import com.gittors.apollo.extend.utils.ApolloExtendUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;

import java.util.List;
import java.util.concurrent.Semaphore;

/**
 * 监听： {@link CommonApolloConstant#APOLLO_EXTEND_ADD_CALLBACK_CONFIG}
 *  配置处理
 * @author zlliu
 * @date 2021/5/8 13:19
 */
@Slf4j
public class ApolloExtendAddListenCallback extends AbstractApolloExtendListenCallback {
    public static final String BEAN_NAME = "apolloExtendAddListenCallback";

    /**
     * 限制同一时间只能一个线程修改 {@link CommonApolloConstant#APOLLO_EXTEND_ADD_CALLBACK_CONFIG} 配置
     */
    private Semaphore semaphore = new Semaphore(1);

    public ApolloExtendAddListenCallback(ApplicationContext applicationContext) {
        super(applicationContext);
    }

    @Override
    protected String getConfigPrefix() {
        return CommonApolloConstant.APOLLO_EXTEND_ADD_CALLBACK_CONFIG;
    }

    @Override
    protected Semaphore lockConfigure() {
        return this.semaphore;
    }

    @Override
    protected boolean check(String managerNamespaceConfig, String managerNamespace) {
        if (StringUtils.isBlank(managerNamespaceConfig)) {
            return true;
        }
        List<String> namespaceList = NAMESPACE_SPLITTER.splitToList(managerNamespaceConfig);
        if (CollectionUtils.isEmpty(namespaceList) || !namespaceList.contains(managerNamespace)) {
            log.warn("#callback managerNamespaceConfig illegal");
            return true;
        }
        return false;
    }

    @Override
    protected ChangeType judgmentChangeType(String managerNamespaceConfig, String managerNamespace) {
        return ChangeType.ADD;
    }

    @Override
    protected ApolloExtendFactory.PropertyFilterPredicate getPropertySourcePredicate(ChangeType changeType) {
        return ApolloExtendUtils.getFilterPredicate(true);
    }
}
