package com.gittors.apollo.extend.spi;

import com.ctrip.framework.apollo.Config;
import lombok.Getter;
import lombok.Setter;
import org.springframework.core.env.PropertySource;

import java.util.Objects;

/**
 * @author zlliu
 * @date 2020/8/23 22:28
 */
@Getter
@Setter
public class ManageNamespaceConfigClass {

    private String namespace;
    /**
     *  Apollo config file object
     */
    private Config config;

    /**
     * PropertySource configuration suffix
     */
    private String compositePropertySourceName;

    private PropertySource configPropertySource;

    /**
     * 管理配置前缀
     */
    private String manageConfigPrefix;

    public ManageNamespaceConfigClass() {
    }

    public ManageNamespaceConfigClass(String namespace, Config config) {
        this.namespace = namespace;
        this.config = config;
    }

    public ManageNamespaceConfigClass(Config config, String compositePropertySourceName) {
        this.config = config;
        this.compositePropertySourceName = compositePropertySourceName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ManageNamespaceConfigClass that = (ManageNamespaceConfigClass) o;
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
                ", compositePropertySourceName='" + compositePropertySourceName + '\'' +
                '}';
    }
}
