#   Apollo Client Extend Starter Gateway Demo

- 说明
```
使用 apollo-client-extend-starter-gateway-adapter 模块的前提：
1、网关的配置纳入 Apollo管理
2、通过新增或修改 Apollo配置，实现路由的动态刷新
```

- 使用
```
依赖 apollo-client-extend-starter-gateway-adapter 模块实现路由的 Spring环境更新
说明：
1、路由组件不限定，可以为 SpringCloud Gateway 或者 Zuul，自行选择

2、由于 apollo-client-extend-starter-gateway-adapter 依赖了 apollo-client-extend-starter
所以网关的路由配置可支持分别放在Apollo的多个命名空间，不限定 application 命名空间
只需在 application 命名空间添加管理配置即可【多个值用","号分割】：apollo.extend.namespace = dynamic-route-config
注：dynamic-route-config 值为其他 Apollo 命名空间名称【具体操作可以在 Apollo 的管理平台的该项目下新建命名空间】

3、网关的路由配置私以为不适合全部放在 Apollo 的application 命名空间，因为接入网关的系统接口如果很多的情况下，application 命名空间的维护量会很大
而且 application 一般只适合放系统级别的配置，比如连接池、Redis、RabbitMQ等，其他的配置最好不要多放，所以可以参考2的做法，新建一个命名空间来管理。

```
