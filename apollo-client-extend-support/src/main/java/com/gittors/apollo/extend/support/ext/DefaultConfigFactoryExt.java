package com.gittors.apollo.extend.support.ext;

import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.ConfigService;
import com.ctrip.framework.apollo.PropertiesCompatibleConfigFile;
import com.ctrip.framework.apollo.build.ApolloInjector;
import com.ctrip.framework.apollo.core.enums.ConfigFileFormat;
import com.ctrip.framework.apollo.internals.LocalFileConfigRepository;
import com.ctrip.framework.apollo.internals.PropertiesCompatibleFileConfigRepository;
import com.ctrip.framework.apollo.internals.RemoteConfigRepository;
import com.ctrip.framework.apollo.spi.DefaultConfigFactory;
import com.ctrip.framework.apollo.util.ConfigUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 静态扩展： {@link DefaultConfigFactory}
 * 修改：
 * 1、{@link #create(String)} 方法对
 *      {@link com.ctrip.framework.apollo.internals.DefaultConfig}
 *      的创建：DefaultConfig --> DefaultConfigExt
 *
 * 此类仅供参考：实际使用 {@link ConfigFactoryProxy} 动态扩展
 *
 * @author zlliu
 * @date 2020/7/26 18:12
 */
@Deprecated
public class DefaultConfigFactoryExt extends DefaultConfigFactory {
    private static final Logger logger = LoggerFactory.getLogger(DefaultConfigFactoryExt.class);

    private ConfigUtil m_configUtil;

    public DefaultConfigFactoryExt() {
        m_configUtil = ApolloInjector.getInstance(ConfigUtil.class);
    }

    @Override
    public Config create(String namespace) {
        ConfigFileFormat format = determineFileFormat(namespace);
        if (ConfigFileFormat.isPropertiesCompatible(format)) {
            return new DefaultConfigExt(namespace, createPropertiesCompatibleFileConfigRepository(namespace, format));
        }
        return new DefaultConfigExt(namespace, createLocalConfigRepository(namespace));
    }

    protected LocalFileConfigRepository createLocalConfigRepository(String namespace) {
        if (m_configUtil.isInLocalMode()) {
            logger.warn(
                    "==== Apollo is in local mode! Won't pull configs from remote server for namespace {} ! ====",
                    namespace);
            return new LocalFileConfigRepository(namespace);
        }
        return new LocalFileConfigRepository(namespace, createRemoteConfigRepository(namespace));
    }

    protected RemoteConfigRepository createRemoteConfigRepository(String namespace) {
        return new RemoteConfigRepository(namespace);
    }

    protected PropertiesCompatibleFileConfigRepository createPropertiesCompatibleFileConfigRepository(String namespace,
                                                                                            ConfigFileFormat format) {
        String actualNamespaceName = trimNamespaceFormat(namespace, format);
        PropertiesCompatibleConfigFile configFile = (PropertiesCompatibleConfigFile) ConfigService
                .getConfigFile(actualNamespaceName, format);

        return new PropertiesCompatibleFileConfigRepository(configFile);
    }

    // for namespaces whose format are not properties, the file extension must be present, e.g. application.yaml
    protected ConfigFileFormat determineFileFormat(String namespaceName) {
        String lowerCase = namespaceName.toLowerCase();
        for (ConfigFileFormat format : ConfigFileFormat.values()) {
            if (lowerCase.endsWith("." + format.getValue())) {
                return format;
            }
        }

        return ConfigFileFormat.Properties;
    }

    String trimNamespaceFormat(String namespaceName, ConfigFileFormat format) {
        String extension = "." + format.getValue();
        if (!namespaceName.toLowerCase().endsWith(extension)) {
            return namespaceName;
        }

        return namespaceName.substring(0, namespaceName.length() - extension.length());
    }
}