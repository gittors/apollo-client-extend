package com.gittors.apollo.extend.service;

import com.gittors.apollo.extend.common.constant.CommonApolloConstant;
import com.gittors.apollo.extend.common.enums.ChangeType;
import com.gittors.apollo.extend.support.ApolloExtendFactory;
import com.gittors.apollo.extend.utils.ApolloExtendUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.List;
import java.util.concurrent.Semaphore;

/**
 * 监听： {@link CommonApolloConstant#APOLLO_EXTEND_GLOBAL_LISTEN_KEY_SUFFIX}
 *  配置处理
 * @author zlliu
 * @date 2021/5/8 13:19
 */
@Slf4j
public class ApolloExtendGlobalListenCallback extends AbstractApolloExtendListenCallback {
    public static final String BEAN_NAME = "apolloExtendGlobalListenCallback";

    /**
     * 限制同一时间只能一个线程修改 {@link CommonApolloConstant#APOLLO_EXTEND_GLOBAL_LISTEN_KEY_SUFFIX} 配置
     */
    private Semaphore semaphore = new Semaphore(1);

    public ApolloExtendGlobalListenCallback(ConfigurableApplicationContext context) {
        super(context);
    }

    @Override
    protected String getConfigPrefix() {
        return CommonApolloConstant.APOLLO_EXTEND_GLOBAL_LISTEN_KEY_SUFFIX;
    }

    @Override
    protected Semaphore lockConfigure() {
        return this.semaphore;
    }

    @Override
    protected ChangeType judgmentChangeType(String managerNamespaceConfig, String managerNamespace) {
        //  没有管理配置,则认为是删除
        if (StringUtils.isBlank(managerNamespaceConfig)) {
            return ChangeType.DELETE;
        }
        List<String> namespaceList = NAMESPACE_SPLITTER.splitToList(managerNamespaceConfig);
        if (CollectionUtils.isEmpty(namespaceList)) {
            return ChangeType.DELETE;
        } else if (CollectionUtils.isNotEmpty(namespaceList) && namespaceList.contains(managerNamespace)) {
            return ChangeType.ADD;
        } else if (CollectionUtils.isNotEmpty(namespaceList) && !namespaceList.contains(managerNamespace)) {
            return ChangeType.DELETE;
        }
        return null;
    }

    @Override
    protected ApolloExtendFactory.PropertyFilterPredicate getPropertySourcePredicate(final ChangeType changeType) {
        ApolloExtendFactory.PropertyFilterPredicate predicate = null;
        switch (changeType) {
            case ADD:
                predicate = ApolloExtendUtils.getFilterPredicate(true);
                break;
            case DELETE:
                predicate = ApolloExtendUtils.getFilterPredicate(false);
                break;
            default:
                predicate = (key, configEntry) -> true;
                break;
        }
        return predicate;
    }

}
