#   Apollo Client Extend : Apollo Client扩展
[![Total lines](https://tokei.rs/b1/github/gittors/apollo-client-extend?category=lines)](https://tokei.rs/b1/github/gittors/apollo-client-extend?category=lines) 
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg?label=license)](https://github.com/gittors/apollo-client-extend/blob/master/LICENSE) 
[![Maven Central](https://img.shields.io/maven-central/v/com.github.gittors/apollo-client-extend.svg?label=maven%20central)](https://search.maven.org/search?q=g:com.github.gittors%20AND%20extend) 
[![Javadocs](http://www.javadoc.io/badge/com.github.gittors/apollo-client-extend.svg)](https://www.javadoc.io/doc/com.github.gittors/apollo-client-extend) 
[![Build Status](https://api.travis-ci.com/gittors/apollo-client-extend.svg?branch=master)](https://travis-ci.com/github/gittors/apollo-client-extend) 
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/8e39a24e1be740c58b83fb81763ba317)](https://app.codacy.com/manual/gittors/apollo-client-extend/dashboard)

## 前言
```
在微服务架构流行的当下，每个服务或多或少会有一些属于应用的配置需要管理，尤其是在灰度场景下，对于配置热更新的需求显得尤为迫切，在这种大背景下，分布式配置中心应运而生。
当下可供选择的分布式配置中心的开源组件有：Apollo、Nacos、SpringCloud Config等。
SpringCloud Config 以GIT仓库 + 消息总线原理，通过手动调用POST请求实现配置热更新，但是没有WEB界面的操作，所以在体验上不尽如人意~
以Apollo和Nacos比较，Apollo的Star 数较高，本项目以 Apollo 为技术选型，在使用者的角度做了一些扩展及思考。
```

## Apollo的一些使用场景：
```
1、用 Apollo做分布式配置中心时，配置的新增和删除操作可以通过其 WEB界面【Apollo Portal】操作，并且 Apollo会联动推送给客户端。
前提是：在 WEB界面管理的命名空间要纳入 Apollo "运行时"的管理范围，方式是通过 启动参数/配置参数或者 Apollo API的一些注解来实现。
比如通过 @EnableApolloConfig 注解：
@Configuration
@EnableApolloConfig({"application","application2"})
public class ApolloConfig {
}
或者通过 application.properties 配置文件的参数：
apollo.bootstrap.namespaces: application,application2 
将命名空间纳入 Apollo管理。
缺点也很明显，就是对通过 WEB界面手动新建的命名空间无能为力，只有通过重启应用并添加到管理范围才能让配置生效。

场景来了：如果我手动新建了一个命名空间，我不想重启服务想让配置生效怎么办？
Apollo：凉拌~ 这不是我的缺点，我不承认这是我的缺点，你不告诉我命名空间，我怎么帮你管理，难道你心中在幻想哪个美女老师我也要提前知道嘛？>_<~
我：咳咳~。。。

2、Apollo 对于 Spring的一些原生注解有很好的支持，比如：@Value
Apollo 的配置如果有更新，会联动 @Value的属性值更新。
但是对 @ConfigurationProperties 的注解支持有限，需要配合 EnvironmentChangeEvent或RefreshScope 使用。

场景来了：我想用 @ConfigurationProperties 注解的时候就跟 @Value 有一样的丝滑体验，不想依赖 EnvironmentChangeEvent或RefreshScope 怎么办？
Apollo：别问我了，我很烦，不知道你哪来这么多奇葩的要求。
我：额。。。
```

## 小目标：
```
为了更好的适配上述的场景，立的小目标：
1、让 Apollo 的命名空间具有"管理"其他命名空间的能力，实现在新增了命名空间的情况下无须重启服务的目标。详见功能描述。
2、支持 @ConfigurationProperties 注解，无须依赖 EnvironmentChangeEvent或RefreshScope。
```

## 功能：
```
1、实现配置管理
例如：application --> application2 : 
application 可管理 application2 的配置项【根据 application2 配置前缀】
备注：
1.1所谓管理，指的是 application 命名空间可以让 application2 命名空间的配置生效或失效。
1.2还可以通过监听 application2的配置前缀，实现 application2 命名空间的配置部分生效或失效。

2、实现 @ConfigurationProperties 注解标注对象的属性值联动更新
```

## 模块说明：
| 模块名 | 描述 |
| --- | --- |
| apollo-client-extend | 基础模块 |
| apollo-client-extend-common | 通用模块 |
| apollo-client-extend-starter | 基础模块Starter |
| apollo-client-extend-event | 事件模块 |
| apollo-client-extend-event-starter | 事件模块Starter |
| apollo-client-extend-binder | 对象绑定模块 |
| apollo-client-extend-binder-starter | 对象绑定模块Starter |
| apollo-client-extend-binder-demo | 绑定模块Demo |
| apollo-client-extend-starter-gateway-adapter | 网关适配模块 |
| apollo-client-extend-starter-gateway-demo | 网关适配模块Demo |
| apollo-client-extend-chain-processor | 链式调用模块 |
| apollo-client-extend-admin | 管理模块 |
| apollo-client-extend-adapter | Apollo版本适配模块 |
| apollo-client-extend-support | 基础支持模块 |


## 依赖版本：
```
Apollo：1.7.0【低于此版本的请参考Apollo版本适配模块说明】
Guava：25.0-jre
SpringBoot：2.2.9.RELEASE
```

## 细则：
1、动态监听某个配置key【value为命名空间的值】，实现新增或删除配置时，刷新 Spring上下文环境
2、且客户端可根据实现接口对新增或删除的配置做相应操作【ApolloExtendCallbackAdapter#changeProcess】
3、@ConfigurationProperties 注解标注的类属性值联动更新

- 细则1具体实现：
```
1、在Apollo application 命名空间新增一个配置项：
apollo.extend.namespace = dynamic-config

备注：dynamic-config 值为另一个 命名空间的名称

2、新增或删除上述定义的配置，动态刷新至 Spring上下文环境
比如新增：
apollo.extend.namespace = dynamic-config,dynamic-config2
那么：
dynamic-config2 新增命名空间的对应配置项会刷新至 Spring上下文环境

比如删除：
apollo.extend.namespace = dynamic-config,dynamic-config2
改为：apollo.extend.namespace = dynamic-config
那么：
dynamic-config2 命名空间的配置项会失效

删除时，如果不配置监听Key，则该命名空间的配置全部失效。
但是也可根据配置的监听Key实现部分失效：
比如配置监听Key：listen.key.delMap.dynamic-config2 = my.map
那么：dynamic-config2 的命名空间以 "my.map" 为前缀的配置将失效。
同理：新增命名空间时，监听 listen.key.addMap.dynamic-config2 = my.map 的配置，可以让其部分生效。
可参考：apollo-client-extend-binder-demo 模块的测试说明。

特别说明：如果客户端需要关心配置的变化，比如新增了配置或删除了配置需要做一些操作的时候：
则需要手动实现：【ApolloExtendCallbackAdapter#changeProcess】 接口
可以参考：apollo-client-extend-binder-demo 模块的 BinderDemoConfiguration 配置类。

```

- 细则3具体实现：
参考：apollo-client-extend-binder 模块

## Apollo Client 版本适配：
```
参考：apollo-client-extend-adapter 模块说明
```

## 使用
```
参考：apollo-client-extend-binder-demo
网关使用参考：apollo-client-extend-starter-gateway-demo
```
