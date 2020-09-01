package com.gittors.apollo.extend.common.manager;

import com.gittors.apollo.extend.common.constant.ApolloExtendAdminConstant;
import com.gittors.apollo.extend.common.enums.TimeUnitEnum;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 缓存管理
 *
 * @author zlliu
 * @date 2019/12/5 17:23
 */
public class CacheManager {

    /**
     * 缓存项最大数量
     */
    @Value("${" + ApolloExtendAdminConstant.CACHE_SIZE + ":50}")
    private long cacheSize;

    /**
     * 缓存时间：1(单位：参考配置项{@link ApolloExtendAdminConstant.TIME_UNIT_STR})
     */
    @Value("${" + ApolloExtendAdminConstant.CACHE_DURATION + ":1}")
    private long cacheDuration;

    /**
     * 缓存对象
     */
    private LoadingCache<String, Object> cache = null;

    private final ReentrantLock lock = new ReentrantLock();

    private Environment environment;

    public CacheManager(Environment environment) {
        this.environment = environment;
    }

    @PostConstruct
    public void initCache() {
        try {
            String timeUnitStr = environment.getProperty(ApolloExtendAdminConstant.TIME_UNIT_STR, TimeUnitEnum.MINUTES.getTimeUnitStr());
            TimeUnit timeUnit = TimeUnitEnum.getTimeUnit(timeUnitStr);
            cache = loadCache(new CacheLoader<String, Object>() {
                @Override
                public Object load(String key) throws Exception {
                    // 处理缓存键不存在缓存值时的处理逻辑
                    return null;
                }
            }, timeUnit);
        } catch (Exception e) {
        }
    }

    private LoadingCache<String, Object> loadCache(CacheLoader<String, Object> cacheLoader, TimeUnit timeUnit) throws Exception {
        LoadingCache<String, Object> cache = CacheBuilder.newBuilder()
                //  缓存池大小，在缓存项接近该大小时， Guava开始回收旧的缓存项
                .maximumSize(cacheSize)
                //  设置时间对象没有被读/写访问则对象从内存中删除
                .expireAfterAccess(cacheDuration, timeUnit)
                //  设置缓存在写入之后 设定时间 后失效
                .expireAfterWrite(cacheDuration, timeUnit)
                //  移除监听器,缓存项被移除时会触发
                .removalListener(new RemovalListener<String, Object>() {
                    @Override
                    public void onRemoval(RemovalNotification<String, Object> rn) {
                        //  逻辑操作
                    }
                })
                //  开启Guava Cache的统计功能
                .recordStats()
                .build(cacheLoader);
        return cache;
    }

    /**
     * 更新缓存对象
     * @param cache
     */
    public void set(LoadingCache<String, Object> cache) {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            this.cache = null;
            this.cache = cache;
        } finally {
            lock.unlock();
        }
    }

    /**
     * 设置缓存值
     * @param key
     * @param value
     */
    public void put(String key, Object value) {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            cache.put(key, value);
        } catch (Exception e) {
        } finally {
            lock.unlock();
        }
    }

    /**
     * 批量设置缓存值
     *
     * @param map
     */
    public void putAll(Map<? extends String, ? extends Object> map) {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            cache.putAll(map);
        } catch (Exception e) {
        } finally {
            lock.unlock();
        }
    }

    /**
     * 获取缓存值
     * @param key
     * @return
     */
    public Object get(String key) {
        Object value = null;
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            value = cache.get(key);
        } catch (Exception e) {
        } finally {
            lock.unlock();
        }
        return value;
    }

    /**
     * 移除缓存
     * @param key
     */
    public void remove(String key) {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            cache.invalidate(key);
        } catch (Exception e) {
        } finally {
            lock.unlock();
        }
    }

    /**
     * 批量移除缓存
     *
     * @param keys
     */
    public void removeAll(Iterable<String> keys) {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            cache.invalidateAll(keys);
        } catch (Exception e) {
        } finally {
            lock.unlock();
        }
    }

    /**
     * 清空所有缓存
     */
    public void removeAll() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            cache.invalidateAll();
        } catch (Exception e) {
        } finally {
            lock.unlock();
        }
    }

    /**
     * 获取缓存项数量
     *
     * @return
     */
    public long size() {
        long size = 0;
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            size = cache.size();
        } catch (Exception e) {
        } finally {
            lock.unlock();
        }
        return size;
    }

}