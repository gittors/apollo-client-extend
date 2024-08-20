package com.gittors.apollo.extend.chain;

import com.gittors.apollo.extend.chain.context.Context;
import com.gittors.apollo.extend.common.constant.CommonApolloConstant;
import com.gittors.apollo.extend.utils.ApolloExtendFileUtils;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author zlliu
 * @date 2020/8/29 2:00
 */
@Slf4j
@Deprecated
public class ApolloExtendNameSpaceFileInjector extends ApolloExtendNameSpaceInjectorAdapter {

    @Override
    public void entry(Context context, ConfigurableApplicationContext applicationContext, Map<String, Object> args) throws Throwable {
        try {
            Map<String, Properties> propertiesMap = getPropertiesMap();
            if (MapUtils.isNotEmpty(propertiesMap)) {
                //  注入命名空间
                doInjector(applicationContext, getAllNamespace(propertiesMap));
            }
        } catch (Exception e) {
            log.error("#entry failed: ", e);
        }

        //  enter into next stream
        fireEntry(context, applicationContext.getEnvironment(), args);
    }

    private Set<String> getAllNamespace(Map<String, Properties> propertiesMap) {
        Set<String> allSet = Sets.newHashSet();

        //  找出自身包涵 "apollo.extend.namespace" 配置项 的命名空间
        Set<String> candidateSet =
        propertiesMap.entrySet()
                .stream()
                .filter(entry -> entry.getValue().containsKey(CommonApolloConstant.APOLLO_EXTEND_NAMESPACE))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());

        allSet.addAll(candidateSet);
        return allSet;
    }

    /**
     * 根据配置文件获得配置信息：
     *      {Key：命名空间，Value：配置文件对象}
     * @return
     */
    private Map<String, Properties> getPropertiesMap() {
        File cacheFile = ApolloExtendFileUtils.findLocalCacheFile();
        File[] tempList = cacheFile.listFiles();
        if (tempList == null || tempList.length <= 0) {
            return Maps.newHashMap();
        }
        Set<File> fileSet =
        Stream.of(tempList)
                .filter(f -> !f.getName().endsWith(
                        Joiner.on(CommonApolloConstant.APOLLO_CONFIG_FILE_SUFFIX)
                        .join(CommonApolloConstant.NAMESPACE_APPLICATION, StringUtils.EMPTY)
                ))
                .collect(Collectors.toSet());

        Map<String, Properties> propertiesMap = loadFromLocalCacheFile(fileSet);
        if (MapUtils.isEmpty(propertiesMap)) {
            return Maps.newHashMap();
        }
        return propertiesMap;
    }

    private Map<String, Properties> loadFromLocalCacheFile(Set<File> fileSet) {
        if (CollectionUtils.isEmpty(fileSet)) {
            return Maps.newHashMap();
        }

        Map<String, Properties> propertiesMap = Maps.newHashMap();
        Properties properties = null;
        for (File file : fileSet) {
            if (file.isFile() && file.canRead()) {
                String namespace = getNamespaceByFileName(file.getName());
                if (StringUtils.isBlank(namespace)) {
                    continue;
                }
                InputStream in = null;
                try {
                    in = new FileInputStream(file);
                    properties = new Properties();
                    properties.load(in);

                    propertiesMap.put(namespace, properties);
                } catch (IOException ex) {
                    log.error("#loadFromLocalCacheFile failed:", ex.getMessage());
                } finally {
                    try {
                        if (in != null) {
                            in.close();
                        }
                    } catch (IOException ex) {
                        // ignore
                    }
                }
            }
        }
        return propertiesMap;
    }

    /**
     * 根据 Apollo缓存文件名获得命名空间名称
     *
     * 比如：apollo-test+default+application.properties --> application
     *
     * @param fileName  文件名
     * @return
     */
    private String getNamespaceByFileName(String fileName) {
        String namespaceFileName =
                Splitter.on(CommonApolloConstant.CLUSTER_NAMESPACE_SEPARATOR)
                        .trimResults()
                        .splitToList(fileName)
                        .stream()
                        .reduce((f1, f2) -> f2)
                        .orElse("");

        String namespace = null;
        if (StringUtils.isNotBlank(namespaceFileName)) {
            namespace = Splitter.on(CommonApolloConstant.APOLLO_CONFIG_FILE_SUFFIX)
                    .trimResults()
                    .splitToList(namespaceFileName)
                    .stream()
                    .reduce((f1, f2) -> f1)
                    .orElse("");
        }
        return namespace;
    }

}
