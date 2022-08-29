# Apollo Client Extend-Admin-Web

## 功能
```
1、通过集成Admin-Web,则可以通过swagger的接口来新增/删除受管理的命名空间
2、通过1的操作,受管理的命名空间的配置会刷新到spring环境中去
```

## 介绍
```
备注：
1、传统SpringMVC项目集成Admin-Web
2、如果是reactor项目,则集成Admin-WebFlux模块

使用：
1、需要开启swagger文档开关：-Dapollo.extend.admin.web.enabled=true
2、文档访问地址：http://ip:port/doc.html
3、如果启动遇到以下错误：
Caused by: java.lang.NoSuchMethodError: com.google.common.collect.FluentIterable.concat
排除自身apollo依赖的guava即可：
<dependency>
    <groupId>com.ctrip.framework.apollo</groupId>
    <artifactId>apollo-client</artifactId>
    <version>${apollo.version}</version>
    <exclusions>
        <exclusion>
            <groupId>com.google.guava</groupId>
            <artifactId>guava</artifactId>
        </exclusion>
    </exclusions>
</dependency>
```
