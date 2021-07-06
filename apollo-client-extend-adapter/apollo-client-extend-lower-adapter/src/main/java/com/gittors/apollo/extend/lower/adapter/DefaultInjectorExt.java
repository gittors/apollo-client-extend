package com.gittors.apollo.extend.lower.adapter;

import com.ctrip.framework.apollo.internals.ConfigManager;
import com.ctrip.framework.apollo.internals.ConfigServiceLocator;
import com.ctrip.framework.apollo.internals.DefaultConfigManager;
import com.ctrip.framework.apollo.internals.RemoteConfigLongPollService;
import com.ctrip.framework.apollo.spi.ConfigFactoryManager;
import com.ctrip.framework.apollo.spi.ConfigRegistry;
import com.ctrip.framework.apollo.spi.DefaultConfigFactory;
import com.ctrip.framework.apollo.spi.DefaultConfigFactoryManager;
import com.ctrip.framework.apollo.spi.DefaultConfigRegistry;
import com.ctrip.framework.apollo.util.ConfigUtil;
import com.ctrip.framework.apollo.util.http.HttpUtil;
import com.ctrip.framework.apollo.util.yaml.YamlParser;
import com.gittors.apollo.extend.common.adapter.AbstractInjector;
import com.google.inject.AbstractModule;
import com.google.inject.Module;
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
public class DefaultInjectorExt extends AbstractInjector {

  @Override
  protected Module[] configureModule() {
    return new Module[] {new ApolloModule()};
  }

  private static class ApolloModule extends AbstractModule {
    @Override
    protected void configure() {
      bind(ConfigManager.class).to(DefaultConfigManager.class).in(Singleton.class);
      bind(ConfigFactoryManager.class).to(DefaultConfigFactoryManager.class).in(Singleton.class);
      bind(ConfigRegistry.class).to(DefaultConfigRegistry.class).in(Singleton.class);
      bind(ConfigUtil.class).in(Singleton.class);
      bind(HttpUtil.class).in(Singleton.class);
      bind(ConfigServiceLocator.class).in(Singleton.class);
      bind(RemoteConfigLongPollService.class).in(Singleton.class);
      bind(YamlParser.class).in(Singleton.class);
    }
  }

}
