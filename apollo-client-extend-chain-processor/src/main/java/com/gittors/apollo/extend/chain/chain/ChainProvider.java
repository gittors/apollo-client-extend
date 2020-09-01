package com.gittors.apollo.extend.chain.chain;

import com.gittors.apollo.extend.chain.spi.ChainBuilder;
import com.gittors.apollo.extend.chain.spi.DefaultChainBuilder;
import com.gittors.apollo.extend.chain.utils.AssertUtils;
import com.gittors.apollo.extend.common.spi.Ordered;
import com.gittors.apollo.extend.common.spi.ServiceLookUp;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zlliu
 * @date 2020/8/14 23:00
 */
public final class ChainProvider {
    private static volatile ChainBuilder chainBuilder = null;

    private static final Map<String, Object> SERVICE_LOADER_MAP = new ConcurrentHashMap<>();

    public static ProcessorChain newChain() {
        if (chainBuilder != null) {
            return chainBuilder.build();
        }
        chainBuilder = loadFirstInstanceOrDefault(ChainBuilder.class, DefaultChainBuilder.class);
        if (chainBuilder == null) {
            chainBuilder = new DefaultChainBuilder();
        }
        return chainBuilder.build();
    }

    public static <T extends Ordered> T loadFirstInstanceOrDefault(Class<T> clazz, Class<? extends T> defaultClass) {
        AssertUtils.notNull(clazz, "SPI class cannot be null");
        AssertUtils.notNull(defaultClass, "default SPI class cannot be null");
        try {
            String key = clazz.getName();
            T service = (T) SERVICE_LOADER_MAP.get(key);
            if (service == null) {
                service = ServiceLookUp.loadPrimary(clazz);
                SERVICE_LOADER_MAP.put(key, service);
            }
            if (service != null) {
                return service;
            }
            return defaultClass.newInstance();
        } catch (Throwable t) {
            return null;
        }
    }

    private ChainProvider() {}
}
