package com.gittors.apollo.extend.chain;

import com.gittors.apollo.extend.chain.context.Context;
import com.gittors.apollo.extend.common.constant.CommonApolloConstant;
import com.gittors.apollo.extend.utils.ApolloExtendUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.env.ConfigurableEnvironment;

import java.util.Map;

/**
 * @author zlliu
 * @date 2020/8/29 2:00
 */
public class ApolloExtendNameSpaceConfigInjector extends ApolloExtendNameSpaceInjectorAdapter {

    @Override
    public void entry(Context context, ConfigurableEnvironment environment, Map<String, Object> args) throws Throwable {
        //  找监控配置：
        //  1、从 Apollo application 命名空间找 - 第一优先级
        //  2、从启动参数 apollo.extend.namespace 配置找 - 第二优先级
        //  3、从 application.properties 的 apollo.bootstrap.namespaces 参数找 - 第三优先级
        String listenerKey = environment.getProperty(CommonApolloConstant.APOLLO_EXTEND_NAMESPACE_PREFIX, CommonApolloConstant.APOLLO_EXTEND_NAMESPACE);
        String managerNamespaces = environment.getProperty(listenerKey);

        if (StringUtils.isNotBlank(managerNamespaces)) {
            doInjector(environment, ApolloExtendUtils.parseNamespace(managerNamespaces));
        }

        //  enter into next stream
        fireEntry(context, environment, args);
    }


}
