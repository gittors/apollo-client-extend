package com.gittors.apollo.extend.common.spi;

import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;

/**
 * @author zlliu
 * @date 2020/8/14 22:50
 */
public class ServiceLookUp {

    public static <T> T loadFirst(Class<T> clazz) {
        Iterator<T> iterator = loadAll(clazz);
        if (!iterator.hasNext()) {
            throw new IllegalStateException(String.format(
                    "No implementation defined in /META-INF/services/%s, please check whether the file exists and has the right implementation class!",
                    clazz.getName()));
        }
        return iterator.next();
    }

    /**
     * 获得所有SPI接口实现
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> Iterator<T> loadAll(Class<T> clazz) {
        ServiceLoader<T> loader = ServiceLoader.load(clazz);
        return loader.iterator();
    }

    /**
     * 加载所有SPI类的实现，按优先级排序
     *
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T extends Ordered> List<T> loadAllOrdered(Class<T> clazz) {
        Iterator<T> iterator = loadAll(clazz);
        if (!iterator.hasNext()) {
            throw new IllegalStateException(String.format(
                    "No implementation defined in /META-INF/services/%s, please check whether the file exists and has the right implementation class!",
                    clazz.getName()));
        }
        List<T> candidates = Lists.newArrayList(iterator);
        // 数值越小优先级越高
        Collections.sort(candidates, Comparator.comparingInt(Ordered::getOrder));
        return candidates;
    }

    /**
     * 加载所有SPI类的实现，选择优先级最高的
     *
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T extends Ordered> T loadPrimary(Class<T> clazz) {
        List<T> candidates = loadAllOrdered(clazz);
        return candidates.get(0);
    }

}
