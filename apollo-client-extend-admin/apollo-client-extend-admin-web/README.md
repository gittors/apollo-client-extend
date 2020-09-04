#   Apollo Client Extend-Admin-Web

## 使用注意事项
```textmate
1、需要开启swagger文档开关：-Dapollo.extend.admin.web.configuration.enabled=true
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
