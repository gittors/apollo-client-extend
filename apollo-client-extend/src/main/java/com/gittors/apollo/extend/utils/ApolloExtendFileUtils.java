package com.gittors.apollo.extend.utils;

import com.ctrip.framework.apollo.build.ApolloInjector;
import com.ctrip.framework.apollo.core.ConfigConsts;
import com.ctrip.framework.apollo.core.utils.ClassLoaderUtil;
import com.ctrip.framework.apollo.internals.LocalFileConfigRepository;
import com.ctrip.framework.apollo.spring.config.ConfigPropertySource;
import com.ctrip.framework.apollo.util.ConfigUtil;
import com.google.common.base.Joiner;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author zlliu
 * @date 2020/7/8 11:29
 */
public final class ApolloExtendFileUtils {
    private ApolloExtendFileUtils() {
    }

    private static final String CONFIG_DIR = "/config-cache";

    /**
     * Apollo 本地缓存文件的绝对目录
     *
     * 参考 Apollo：
     *      {@link LocalFileConfigRepository#assembleLocalCacheFile(File, String)}
     *      {@link LocalFileConfigRepository#findLocalCacheDir()}
     * @return
     */
    public static String findLocalCacheDir() {
        boolean win = new ConfigUtil().isOSWindows();
        try {
            ConfigUtil m_configUtil = ApolloInjector.getInstance(ConfigUtil.class);
            //  /opt/data
            String defaultCacheDir = m_configUtil.getDefaultLocalCacheDir();
            Path path = Paths.get(defaultCacheDir);
            if (!Files.exists(path)) {
                return null;
            }
            if (Files.exists(path) && Files.isWritable(path)) {
                if (win) {
                    return defaultCacheDir + "\\" + CONFIG_DIR;
                } else {
                    return defaultCacheDir + "/" + CONFIG_DIR;
                }
            }
        } catch (Throwable ex) {
            //ignore
        }
        if (win) {
            return ClassLoaderUtil.getClassPath() + "\\" + CONFIG_DIR;
        } else {
            return ClassLoaderUtil.getClassPath() + "/" + CONFIG_DIR;
        }
    }

    public static File findLocalCacheFile() {
        try {
            String defaultCacheDir = ApolloInjector.getInstance(ConfigUtil.class).getDefaultLocalCacheDir();
            Path path = Paths.get(defaultCacheDir);
            if (!Files.exists(path)) {
                Files.createDirectories(path);
            }
            if (Files.exists(path) && Files.isWritable(path)) {
                return new File(defaultCacheDir, CONFIG_DIR);
            }
        } catch (Throwable ex) {
            //ignore
        }
        return new File(ClassLoaderUtil.getClassPath(), CONFIG_DIR);
    }

    public static String finalFileName(String fileName) {
        String path = findLocalCacheDir();
        boolean win = new ConfigUtil().isOSWindows();
        if (win) {
            return path + "\\" + fileName;
        } else {
            return path + "/" + fileName;
        }
    }

    /**
     * 删除Apollo缓存文件
     *
     * @param configPropertySource
     */
    @Deprecated
    public void deleteLocalFile(ConfigPropertySource configPropertySource) {
        ConfigUtil m_configUtil = ApolloInjector.getInstance(ConfigUtil.class);
        String fileName =
                String.format("%s.properties", Joiner.on(ConfigConsts.CLUSTER_NAMESPACE_SEPARATOR)
                        .join(m_configUtil.getAppId(), m_configUtil.getCluster(), configPropertySource.getName()));
        try {
            //  删除文件
            Files.deleteIfExists(Paths.get(ApolloExtendFileUtils.finalFileName(fileName)));
        } catch (IOException e) {
        }
    }

}
