# Apollo Client Extend Adapter

## Apollo 版本适配

-   1.6.x ~ 1.7.x的版本：【无需适配, 依赖apollo-client-extend-starter即可】
```textmate
<dependency>
    <groupId>com.github.gittors</groupId>
    <artifactId>apollo-client-extend-starter</artifactId>
    <version>1.1.1</version>
</dependency>
```

-   1.3.x ~ 1.5.x的版本：【适配低版本】
```textmate
<dependency>
    <groupId>com.github.gittors</groupId>
    <artifactId>apollo-client-extend-starter</artifactId>
    <version>1.1.1</version>
    <exclusions>
        <exclusion>
            <groupId>com.github.gittors</groupId>
            <artifactId>apollo-client-extend-higher-adapter</artifactId>
        </exclusion>
    </exclusions>
</dependency>

<dependency>
    <groupId>com.github.gittors</groupId>
    <artifactId>apollo-client-extend-lower-adapter</artifactId>
    <version>1.1.1</version>
</dependency>
```

```
注：版本请依赖最新的
```