package com.gittors.apollo.extend.support.ext;

import com.ctrip.framework.apollo.core.utils.ClassLoaderUtil;
import com.ctrip.framework.apollo.enums.ConfigSourceType;
import com.ctrip.framework.apollo.enums.PropertyChangeType;
import com.ctrip.framework.apollo.internals.ConfigRepository;
import com.ctrip.framework.apollo.internals.RepositoryChangeListener;
import com.ctrip.framework.apollo.model.ConfigChange;
import com.ctrip.framework.apollo.model.ConfigChangeEvent;
import com.ctrip.framework.apollo.tracer.Tracer;
import com.ctrip.framework.apollo.util.ExceptionUtil;
import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.RateLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 【静态扩展点】
 *    1、开放 {@link #updateConfig(Properties, ConfigSourceType)} 方法权限： private --> public synchronized
 *    2、开放 {@link #initialize()} 方法权限： private --> public
 *          新增回调属性 {@link propertiesCallBack} 的 finally 设置为空的逻辑
 *    3、新增回调属性 {@link propertiesCallBack} + {@link #addPropertiesCallBack(PropertiesCallBack)} 方法: 配置删除时用
 *    4、新增属性更新方法 {@link #setProperty(String, String)}
 *    5、新增属性 m_configRepository GET方法 {@link #getConfigRepository()}
 *    6、方法 {@link #onRepositoryChange(String, Properties)}} 新增配置回调逻辑
 *
 *    注意：
 *        1.如果要扩展，请在 {@link com.ctrip.framework.apollo.internals.DefaultConfig} 基础上做扩展，而不是在此类基础上！！
 *        (意思是将源代码拷贝出来修改，而不是针对此类修改)
 *        2.如果要扩展，请务必实现上述列出来的扩展点！！
 *
 * 此类是静态扩展，动态扩展请参考： {@link ConfigFactoryProxy}
 *
 * @author zlliu
 * @date 2020/7/25 11:18
 */
@Deprecated
public class DefaultConfigExt extends AbstractConfigExt implements RepositoryChangeListener {
  private static final Logger logger = LoggerFactory.getLogger(DefaultConfigExt.class);
  private final String m_namespace;
  private final Properties m_resourceProperties;
  private final AtomicReference<Properties> m_configProperties;
  private final ConfigRepository m_configRepository;
  private final RateLimiter m_warnLogRateLimiter;

  //  ++配置回调
  private final AtomicReference<PropertiesCallBack> propertiesCallBack
          = new AtomicReference<>();;


  private volatile ConfigSourceType m_sourceType = ConfigSourceType.NONE;

  public synchronized void setProperty(String key, String value) {
    if (m_configProperties.get() != null) {
      m_configProperties.get().setProperty(key, value);
    }
  }

  /**
   * Constructor.
   *
   * @param namespace        the namespace of this config instance
   * @param configRepository the config repository for this config instance
   */
  public DefaultConfigExt(String namespace, ConfigRepository configRepository) {
    m_namespace = namespace;
    m_resourceProperties = loadFromResource(m_namespace);
    m_configRepository = configRepository;
    m_configProperties = new AtomicReference<>();
    m_warnLogRateLimiter = RateLimiter.create(0.017); // 1 warning log output per minute
    initialize();
  }

  public ConfigRepository getConfigRepository() {
    return this.m_configRepository;
  }

  public void initialize() {
    try {
      updateConfig(m_configRepository.getConfig(), m_configRepository.getSourceType());
    } catch (Throwable ex) {
      Tracer.logError(ex);
      logger.warn("Init Apollo Local Config failed - namespace: {}, reason: {}.",
          m_namespace, ExceptionUtil.getDetailMessage(ex));
    } finally {
      //register the change listener no matter config repository is working or not
      //so that whenever config repository is recovered, config could get changed
      m_configRepository.addChangeListener(this);

      //  ++配置回调设置为空
      propertiesCallBack.set(null);
    }
  }

  public synchronized void addPropertiesCallBack(PropertiesCallBack callBack) {
    this.propertiesCallBack.set(callBack);
  }

  @Override
  public String getProperty(String key, String defaultValue) {
    // step 1: check system properties, i.e. -Dkey=value
    String value = System.getProperty(key);

    // step 2: check local cached properties file
    if (value == null && m_configProperties.get() != null) {
      value = m_configProperties.get().getProperty(key);
    }

    /**
     * step 3: check env variable, i.e. PATH=...
     * normally system environment variables are in UPPERCASE, however there might be exceptions.
     * so the caller should provide the key in the right case
     */
    if (value == null) {
      value = System.getenv(key);
    }

    // step 4: check properties file from classpath
    if (value == null && m_resourceProperties != null) {
      value = (String) m_resourceProperties.get(key);
    }

    if (value == null && m_configProperties.get() == null && m_warnLogRateLimiter.tryAcquire()) {
      logger.warn("Could not load config for namespace {} from Apollo, please check whether the configs are released in Apollo! Return default value now!", m_namespace);
    }

    return value == null ? defaultValue : value;
  }

  @Override
  public Set<String> getPropertyNames() {
    Properties properties = m_configProperties.get();
    if (properties == null) {
      return Collections.emptySet();
    }

    return stringPropertyNames(properties);
  }

  @Override
  public ConfigSourceType getSourceType() {
    return m_sourceType;
  }

  private Set<String> stringPropertyNames(Properties properties) {
    //jdk9以下版本Properties#enumerateStringProperties方法存在性能问题，keys() + get(k) 重复迭代, jdk9之后改为entrySet遍历.
    Map<String, String> h = new HashMap<>();
    for (Map.Entry<Object, Object> e : properties.entrySet()) {
      Object k = e.getKey();
      Object v = e.getValue();
      if (k instanceof String && v instanceof String) {
        h.put((String) k, (String) v);
      }
    }
    return h.keySet();
  }

  /**
   * 更新内存对象 和 回调自定义监听器
   * updateAndCalcConfigChanges(newConfigProperties, sourceType)：更新内存对象
   * this.fireConfigChange(new ConfigChangeEvent(m_namespace, actualChanges))：回调自定义监听器
   *
   * @param namespace
   * @param newProperties
   */
  @Override
  public synchronized void onRepositoryChange(String namespace, Properties newProperties) {
    if (newProperties.equals(m_configProperties.get())) {
      return;
    }

    //  ++新增配置回调逻辑
    if (propertiesCallBack.get() != null) {
      newProperties = (Properties) propertiesCallBack.get().callBack(newProperties);
    }

    ConfigSourceType sourceType = m_configRepository.getSourceType();
    Properties newConfigProperties = new Properties();
    newConfigProperties.putAll(newProperties);

    Map<String, ConfigChange> actualChanges = updateAndCalcConfigChanges(newConfigProperties, sourceType);

    //check double checked result
    if (actualChanges.isEmpty()) {
      return;
    }

    this.fireConfigChange(new ConfigChangeEvent(m_namespace, actualChanges));

    Tracer.logEvent("Apollo.Client.ConfigChanges", m_namespace);
  }

  public synchronized void updateConfig(Properties newConfigProperties, ConfigSourceType sourceType) {
    m_configProperties.set(newConfigProperties);
    m_sourceType = sourceType;
  }

  private Map<String, ConfigChange> updateAndCalcConfigChanges(Properties newConfigProperties,
      ConfigSourceType sourceType) {
    List<ConfigChange> configChanges =
        calcPropertyChanges(m_namespace, m_configProperties.get(), newConfigProperties);

    ImmutableMap.Builder<String, ConfigChange> actualChanges =
        new ImmutableMap.Builder<>();

    /** === Double check since DefaultConfig has multiple config sources ==== **/

    //1. use getProperty to update configChanges's old value
    for (ConfigChange change : configChanges) {
      change.setOldValue(this.getProperty(change.getPropertyName(), change.getOldValue()));
    }

    //2. update m_configProperties
    updateConfig(newConfigProperties, sourceType);
    clearConfigCache();

    //3. use getProperty to update configChange's new value and calc the final changes
    for (ConfigChange change : configChanges) {
      change.setNewValue(this.getProperty(change.getPropertyName(), change.getNewValue()));
      switch (change.getChangeType()) {
        case ADDED:
          if (Objects.equals(change.getOldValue(), change.getNewValue())) {
            break;
          }
          if (change.getOldValue() != null) {
            change.setChangeType(PropertyChangeType.MODIFIED);
          }
          actualChanges.put(change.getPropertyName(), change);
          break;
        case MODIFIED:
          if (!Objects.equals(change.getOldValue(), change.getNewValue())) {
            actualChanges.put(change.getPropertyName(), change);
          }
          break;
        case DELETED:
          if (Objects.equals(change.getOldValue(), change.getNewValue())) {
            break;
          }
          if (change.getNewValue() != null) {
            change.setChangeType(PropertyChangeType.MODIFIED);
          }
          actualChanges.put(change.getPropertyName(), change);
          break;
        default:
          //do nothing
          break;
      }
    }
    return actualChanges.build();
  }

  private Properties loadFromResource(String namespace) {
    String name = String.format("META-INF/config/%s.properties", namespace);
    InputStream in = ClassLoaderUtil.getLoader().getResourceAsStream(name);
    Properties properties = null;

    if (in != null) {
      properties = new Properties();

      try {
        properties.load(in);
      } catch (IOException ex) {
        Tracer.logError(ex);
        logger.error("Load resource config for namespace {} failed", namespace, ex);
      } finally {
        try {
          in.close();
        } catch (IOException ex) {
          // ignore
        }
      }
    }

    return properties;
  }
}
