package com.gittors.apollo.extend.lower.adapter;

import com.ctrip.framework.apollo.exceptions.ApolloConfigException;
import com.ctrip.framework.apollo.internals.ConfigManager;
import com.ctrip.framework.apollo.internals.ConfigServiceLocator;
import com.ctrip.framework.apollo.internals.DefaultConfigManager;
import com.ctrip.framework.apollo.internals.Injector;
import com.ctrip.framework.apollo.internals.RemoteConfigLongPollService;
import com.ctrip.framework.apollo.spi.ConfigFactory;
import com.ctrip.framework.apollo.spi.ConfigFactoryManager;
import com.ctrip.framework.apollo.spi.ConfigRegistry;
import com.ctrip.framework.apollo.spi.DefaultConfigFactory;
import com.ctrip.framework.apollo.spi.DefaultConfigFactoryManager;
import com.ctrip.framework.apollo.spi.DefaultConfigRegistry;
import com.ctrip.framework.apollo.tracer.Tracer;
import com.ctrip.framework.apollo.util.ConfigUtil;
import com.ctrip.framework.apollo.util.http.HttpUtil;
import com.ctrip.framework.apollo.util.yaml.YamlParser;
import com.gittors.apollo.extend.support.ext.DefaultConfigFactoryExt;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Singleton;

/**
 * 扩展：
 *  {@link com.ctrip.framework.apollo.internals.DefaultInjector}
 *
 *  修改 {@link ApolloModule#configure()} 的方法
 *  对 {@link DefaultConfigFactory} 的注入：DefaultConfigFactory --> DefaultConfigFactoryExt
 *
 * @author zlliu
 * @date 2020/7/26 18:12
 */
public class DefaultInjectorExt implements Injector {
  private com.google.inject.Injector m_injector;

  public DefaultInjectorExt() {
    try {
      m_injector = Guice.createInjector(new ApolloModule(), new ApolloExtendModule());
    } catch (Throwable ex) {
      ApolloConfigException exception = new ApolloConfigException("Unable to initialize Guice Injector!", ex);
      Tracer.logError(exception);
      throw exception;
    }
  }

  @Override
  public <T> T getInstance(Class<T> clazz) {
    try {
      return m_injector.getInstance(clazz);
    } catch (Throwable ex) {
      Tracer.logError(ex);
      throw new ApolloConfigException(
          String.format("Unable to load instance for %s!", clazz.getName()), ex);
    }
  }

  @Override
  public <T> T getInstance(Class<T> clazz, String name) {
    //Guice does not support get instance by type and name
    return null;
  }

  /**
   * 修改 {@link DefaultConfigFactory} 注入
   */
  private static class ApolloModule extends AbstractModule {
    @Override
    protected void configure() {
      bind(ConfigManager.class).to(DefaultConfigManager.class).in(Singleton.class);
      bind(ConfigFactoryManager.class).to(DefaultConfigFactoryManager.class).in(Singleton.class);
      bind(ConfigRegistry.class).to(DefaultConfigRegistry.class).in(Singleton.class);
      bind(ConfigFactory.class).to(DefaultConfigFactoryExt.class).in(Singleton.class);
      bind(ConfigUtil.class).in(Singleton.class);
      bind(HttpUtil.class).in(Singleton.class);
      bind(ConfigServiceLocator.class).in(Singleton.class);
      bind(RemoteConfigLongPollService.class).in(Singleton.class);
      bind(YamlParser.class).in(Singleton.class);
    }
  }

  private static class ApolloExtendModule extends AbstractModule {
    @Override
    protected void configure() {
    }
  }
}
