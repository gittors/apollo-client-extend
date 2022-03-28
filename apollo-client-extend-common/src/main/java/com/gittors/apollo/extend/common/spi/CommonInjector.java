package com.gittors.apollo.extend.common.spi;

/**
 * @author zlliu
 * @date 2022/03/28 12:13
 */
public class CommonInjector {
    private static volatile Injector s_injector;
    private static final Object lock = new Object();

    private static Injector getInjector() {
        if (s_injector == null) {
            synchronized (lock) {
                if (s_injector == null) {
                    try {
                        s_injector = ServiceLookUp.loadPrimary(Injector.class);
                    } catch (Throwable ex) {
                        throw ex;
                    }
                }
            }
        }
        return s_injector;
    }

    public static <T> T getInstance(Class<T> clazz) {
        try {
            return getInjector().getInstance(clazz);
        } catch (Throwable ex) {
            throw ex;
        }
    }

    public static <T> T getInstance(Class<T> clazz, String name) {
        try {
            return getInjector().getInstance(clazz, name);
        } catch (Throwable ex) {
            throw ex;
        }
    }
}
