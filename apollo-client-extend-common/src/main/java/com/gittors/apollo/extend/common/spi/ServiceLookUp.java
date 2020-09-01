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

    public static <T> Iterator<T> loadAll(Class<T> clazz) {
        ServiceLoader<T> loader = ServiceLoader.load(clazz);
        return loader.iterator();
    }

    public static <T extends Ordered> List<T> loadAllOrdered(Class<T> clazz) {
        Iterator<T> iterator = loadAll(clazz);
        if (!iterator.hasNext()) {
            throw new IllegalStateException(String.format(
                    "No implementation defined in /META-INF/services/%s, please check whether the file exists and has the right implementation class!",
                    clazz.getName()));
        }
        List<T> candidates = Lists.newArrayList(iterator);
        Collections.sort(candidates, new Comparator<T>() {
            @Override
            public int compare(T o1, T o2) {
                // the smaller order has higher priority
                return Integer.compare(o1.getOrder(), o2.getOrder());
            }
        });
        return candidates;
    }

    public static <T extends Ordered> T loadPrimary(Class<T> clazz) {
        List<T> candidates = loadAllOrdered(clazz);
        return candidates.get(0);
    }

}
