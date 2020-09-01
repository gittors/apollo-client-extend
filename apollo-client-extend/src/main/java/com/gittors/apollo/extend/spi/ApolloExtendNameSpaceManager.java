package com.gittors.apollo.extend.spi;

import com.gittors.apollo.extend.common.spi.Ordered;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.core.env.ConfigurableEnvironment;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author zlliu
 * @date 2020/8/26 10:33
 */
public interface ApolloExtendNameSpaceManager<T> extends Ordered {

    /**
     * 设置环境
     * @param environment
     */
    void setEnvironment(ConfigurableEnvironment environment);

    /**
     * 设置bean工厂
     * @param beanFactory
     */
    void setBeanFactory(BeanFactory beanFactory);

    /**
     * 得到新增命名空间
     * @param needAddNamespaceSet
     * @return
     */
    List<Map<String, String>> getAddNamespace(Set<String> needAddNamespaceSet);

    /**
     * 新增命名空间
     * @param list
     * @param managerConfigPrefix
     */
    void addNamespace(List<T> list, Map.Entry<Boolean, Set<String>> managerConfigPrefix);

    /**
     * 得到需删除的命名空间
     * @param needDeleteNamespaceSet
     * @return
     */
    List<Map<String, String>> getDeleteNamespace(Set<String> needDeleteNamespaceSet);

    /**
     * 删除命名空间
     * @param list
     * @param managerConfigPrefix
     */
    void deleteNamespace(List<T> list, Map.Entry<Boolean, Set<String>> managerConfigPrefix);
}
