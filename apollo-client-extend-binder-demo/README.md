# Apollo Client Extend Binder Demo

-   apollo-client-extend-binder 模块可单独使用，也可以复合 apollo-client-extend 模块使用：
-   复合使用可实现配置放在多个命名空间，且联动更新
````text
单独使用时，只需依赖 apollo-client-extend-binder 即可：
依赖项：
<dependency>
    <groupId>com.gittors</groupId>
    <artifactId>apollo-client-extend-starter</artifactId>
    <version>1.0.1</version>
</dependency>

复合使用时，依赖 apollo-client-extend-binder 和 apollo-client-extend：
依赖项：
<dependency>
    <groupId>com.gittors</groupId>
    <artifactId>apollo-client-extend-starter</artifactId>
    <version>1.0.1</version>
</dependency>

<dependency>
    <groupId>com.gittors</groupId>
    <artifactId>apollo-client-extend-binder-starter</artifactId>
    <version>1.0.1</version>
</dependency>

说明：
由于依赖了 apollo-client-extend-starter，所以在 application 命名空间添加配置项即可实现配置管理【多个值用","号分割】：
apollo.extend.namespace = application-test
注：application-test 值为其他 命名空间名称
````

测试
````text
1、在 Apollo WEB 新建 application-test 命名空间
2、application 和 application-test 命名空间的配置，可参考：/config/application.txt 和 /config/application-test.txt
3、通过修改 application 的 apollo.extend.namespace 配置属性：删除或新增【新增要保证有其他命名空间的情况下】
可观察 MyProperties.java 对象的属性值是否联动更新【通过 BinderDemoController 的get接口】。
````