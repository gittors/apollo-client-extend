# Apollo Client Extend : Apollo Client扩展

[![Total lines](https://tokei.rs/b1/github/gittors/apollo-client-extend?category=lines)](https://tokei.rs/b1/github/gittors/apollo-client-extend?category=lines) 
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg?label=license)](https://github.com/gittors/apollo-client-extend/blob/master/LICENSE) 
[![Maven Central](https://img.shields.io/maven-central/v/com.github.gittors/apollo-client-extend.svg?label=maven%20central)](https://search.maven.org/search?q=g:com.github.gittors%20AND%20extend) 
[![Javadocs](http://www.javadoc.io/badge/com.github.gittors/apollo-client-extend.svg)](https://www.javadoc.io/doc/com.github.gittors/apollo-client-extend) 
[![Build Status](https://api.travis-ci.com/gittors/apollo-client-extend.svg?branch=master)](https://travis-ci.com/github/gittors/apollo-client-extend) 
[![Codacy Badge](https://app.codacy.com/project/badge/Grade/3da0bc583b1d439586401f2469d9ac5e)](https://www.codacy.com/manual/gittors/apollo-client-extend?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=gittors/apollo-client-extend&amp;utm_campaign=Badge_Grade)

## 前言
```textmate
在微服务架构流行的当下，每个服务或多或少会有一些属于应用的配置需要管理，尤其在灰度场景下，对于配置热更新的需求显得尤为迫切，在这种背景下，分布式配置中心应运而生。
当下可供选择的分布式配置中心的开源组件有：SpringCloud Config、Apollo、Nacos等。
本项目以 Apollo 为技术选型，在使用者的角度做了一些扩展及思考。
```

## Apollo 官网
https://github.com/ctripcorp/apollo

## Apollo的一些使用场景：
```textmate
1、用 Apollo做分布式配置中心时，配置的新增和删除操作可以通过其 WEB界面【Apollo Portal】操作，并且配置的更改 Apollo会联动推送给每个客户端。
前提是：在 WEB管理的命名空间要纳入 Apollo "运行时态"的管理范围，方式是通过 启动参数/配置参数或者 Apollo 的一些API来实现。
比如通过 @EnableApolloConfig 注解：
@Configuration
@EnableApolloConfig({"application","application2"})
public class ApolloConfig {
}
或者通过 application.properties 配置文件的参数：
apollo.bootstrap.namespaces: application,application2 
将【application,application2】命名空间选择性的纳入 Apollo的管理范围。
缺点就是，对于手动新建的命名空间只能通过重启服务并添加到管理范围才能让配置生效。

场景1：如果我手动新建了一个命名空间，我不想重启服务就想让配置生效怎么办？

2、Apollo 对于 Spring的一些原生注解有很好的支持，比如：@Value 注解。
如果配置有更新，Apollo 会联动 @Value的属性值更新。
但是对 @ConfigurationProperties 注解的支持有限，需要配合 EnvironmentChangeEvent 或 RefreshScope 使用。

场景2：我想用 @ConfigurationProperties 注解的时候就跟 @Value 有一样的丝滑体验，不想依赖 EnvironmentChangeEvent或RefreshScope 怎么办？

还有一些场景：
1、比如某一类型的配置，我不想放在同一个命名空间内管理，这样既增加了维护的难度也显得配置很臃肿，我希望根据某一维度分开管理。【比如网关的路由配置，我希望根据接入的系统分开管理，而不是放在一起】
2、比如某个命名空间内的配置，在特定的场景下，我希望让一部分配置失效，而不影响其他的配置状态。

```

## 小目标：
* **为了更好的适配上述的一些使用场景，是这个项目的诞生背景，以下是一些小目标：**
  * 实现在新增了命名空间的情况下无须重启服务就让配置生效【但需要将命名空间名称添加到对应的配置】。
  * 支持 @ConfigurationProperties 注解，无须依赖 EnvironmentChangeEvent 或 RefreshScope。
  * 实现让 Apollo 的命名空间具有部分 "管理" 其他命名空间的能力【详见功能描述】。
  * 基于3，实现配置的可部分生效/可部分失效。

## 功能描述：
* **实现在新增了命名空间的情况下无须重启服务就让配置生效**
  * 需要在配置项：apollo.extend.namespace 添加新建的命名空间名称

* **实现 @ConfigurationProperties 注解标注对象的属性值联动更新**
  * 对于 @ConfigurationProperties 注解标注的对象，如果相应的配置有更新，则联动更新其属性值


* **实现配置管理**
  * 新增了命名空间无须重启服务
```textmate
例如：application 命名空间新增配置项：apollo.extend.namespace=application2
则 application 可管理 application2 的配置项, 可以管理多个命名空间, 配置多个值: "," 号分隔。
新增或者删除上述的配置,即代表相应的配置生效或失效。
备注：
1所谓管理，指的是 application 命名空间可以让 application2 命名空间的配置生效或失效功能。
2还可以通过监听 application2的配置前缀，实现 application2 命名空间的配置部分生效或失效功能。

```

## 模块说明：
| 模块名 | 描述 |
| --- | --- |
| apollo-client-extend | 基础模块 |
| apollo-client-extend-common | 通用模块 |
| apollo-client-extend-starter | 基础模块Starter |
| apollo-client-extend-event | 事件模块 |
| apollo-client-extend-event-starter | 事件模块Starter |
| apollo-client-extend-binder | 对象绑定 |
| apollo-client-extend-binder-starter | 对象绑定Starter |
| apollo-client-extend-demo | 示例Demo |
| apollo-client-extend-starter-gateway-adapter | 网关适配 |
| apollo-client-extend-chain-processor | 链式调用 |
| apollo-client-extend-admin | 管理模块 |
| apollo-client-extend-adapter | Apollo Client版本适配 |
| apollo-client-extend-support | 基础支持模块 |


## 依赖版本：
```textmate
Apollo：1.7.0【低于此版本的请参考Apollo版本适配说明】
Guava：30.0-jre
SpringBoot：2.2.9.RELEASE
```

## 实现细则：
* **通过监听管理配置，实现新增或删除命名空间时，将命名空间的配置刷新至服务环境，无须重启服务**
* **新增或删除管理配置时，可让命名空间内的配置部分生效或失效**
* **@ConfigurationProperties 注解标注的类属性值联动更新**

 **细则1实现：**

```textmate
1、在Apollo application 命名空间新增一个配置项：
apollo.extend.namespace = dynamic-config

2、新增或删除上述定义的 apollo.extend.namespace 配置，动态刷新至服务环境
比如新增：
apollo.extend.namespace = dynamic-config,dynamic-config2
那么：
dynamic-config2 新增命名空间的对应配置项会刷新至 服务环境

比如删除：
apollo.extend.namespace = dynamic-config,dynamic-config2
改为：apollo.extend.namespace = dynamic-config
那么：
dynamic-config2 命名空间的配置会失效

实现上，通过监听 apollo.extend.namespace 配置的变化，将对应命名空间的配置刷新。


特别说明：如果客户端需要关心配置的变化，比如新增了配置或删除了配置需要做一些操作的时候：
则需要手动实现：【ApolloExtendCallbackAdapter#changeProcess】 接口
可以参考：apollo-client-extend-binder-demo 模块的 BinderDemoConfiguration 配置类。

```

 **细则2实现：**
```textmate
控制管理的命名空间的配置, 部分生效或失效：
增加配置项：
## 配置当前命名空间所管理命名空间的监听key - 新增
listen.key.addMap.application2 = my.map
说明：添加 application2 的管理配置时, 其以 "my.map" 为前缀的配置生效。

## 配置当前命名空间所管理命名空间的监听key - 删除
listen.key.delMap.application2 = my1.map
说明：删除 application2 的管理配置时, 其以 "my1.map" 为前缀的配置失效。

## 配置当前命名空间所管理命名空间的监听key - 全局
listen.key.global.map.application2 = my.map3
说明：此为管理命名空间的全局配置, 新增或删除时都将生效
当新增管理配置 application2 时: application2 命名空间的生效配置：my.map3,my.map
当删除管理配置 application2 时: application2 命名空间的失效配置：my.map3,my1.map

备注：如果不配置上述配置, 则新增/删除命名空间时配置全部生效/全部失效。
可参考：apollo-client-extend-binder-demo 模块的测试说明。
```

 **细则3实现：**
参考：apollo-client-extend-binder 模块

## Apollo Client 版本适配：
```textmate
参考：apollo-client-extend-adapter 模块说明
```

## 使用
```textmate
参考：apollo-client-extend-binder-demo
网关使用参考：apollo-client-extend-starter-gateway-demo
```
