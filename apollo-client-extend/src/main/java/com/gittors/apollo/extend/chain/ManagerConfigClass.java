package com.gittors.apollo.extend.chain;

import com.ctrip.framework.apollo.Config;
import com.gittors.apollo.extend.common.env.SimplePropertySource;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

/**
 * @author zlliu
 * @date 2020/8/23 22:28
 */
@Getter
@Setter
public class ManagerConfigClass {

    private String namespace;
    /**
     *  Apollo config file object
     */
    private Config config;

    /**
     * 管理配置前缀
     */
    private String manageConfigPrefix;

    private SimplePropertySource simplePropertySource;

    public ManagerConfigClass(String namespace, Config config) {
        this.namespace = namespace;
        this.config = config;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ManagerConfigClass that = (ManagerConfigClass) obj;
        return Objects.equals(namespace, that.namespace);
    }

    @Override
    public int hashCode() {
        return Objects.hash(namespace);
    }

    @Override
    public String toString() {
        return "ManageNamespaceConfigClass{" +
                "namespace='" + namespace + '\'' +
                '}';
    }
}
