package com.gittors.apollo.extend.spi;

import com.gittors.apollo.extend.common.context.ApolloPropertySourceContext;
import com.gittors.apollo.extend.common.enums.ChangeType;
import com.gittors.apollo.extend.common.service.Ordered;
import com.gittors.apollo.extend.support.ApolloExtendFactory;
import com.gittors.apollo.extend.utils.ApolloExtendUtils;
import org.springframework.core.env.ConfigurableEnvironment;

import java.util.Map;
import java.util.Set;

/**
 * 【这个缺省的实现】：
 * 1、假如配置了：listen.key.addMap.application2 = my.key
 * 2、即根据1的配置，将命名空间 application2的配置部分生效，生效的KEY为"my.key"
 * 注：如果不需要这个功能(加载全部key)，通过SPI替换一个空的实现即可
 *
 * 如果配置了 addMap 和 delMap 的配置，初始化时只有 addMap 生效(后续修改 delMap 的配置，只是在addMap基础上做删除)：
 * ## 配置当前命名空间所管理命名空间的监听key - 新增
 * listen.key.addMap.application-test = my.map,my1.map1
 * ## 配置当前命名空间所管理命名空间的监听key - 删除
 * listen.key.delMap.application-test = my.map,my1.map1
 *
 * @author zlliu
 * @date 2020/9/3 11:19
 */
public class DefaultExtendManageNamespacePostProcessor implements ApolloExtendManageNamespacePostProcessor {

    @Override
    public void postProcessNamespaceManager(ConfigurableEnvironment environment) {
        Set<String> namespaceSet = ApolloPropertySourceContext.INSTANCE.getSourceNamespace();
        //  获得管理配置，部分配置生效等
        Map<String, Map.Entry<Boolean, Set<String>>> managerConfigMap =
                ApolloExtendUtils.getManagerConfig(environment, namespaceSet, ChangeType.ADD);

        ApolloExtendFactory.PropertyFilterPredicate filterPredicate = ApolloExtendUtils.getFilterPredicate(true);

        //  过滤Apollo配置，使其部分生效
        ApolloPropertySourceContext.INSTANCE.getPropertySources().forEach(propertySource ->
                ApolloExtendUtils.configValidHandler(propertySource, managerConfigMap.get(propertySource.getNamespace()), filterPredicate)
        );

    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
