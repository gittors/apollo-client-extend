# Apollo Client Extension Binder

```textmate
问题：
Apollo 新增或修改的配置项，不能联动更新到 @ConfigurationProperties 注解标注的类
```

-   功能：
```textmate
1、实现Apollo配置修改时，联动更新 @ConfigurationProperties 标注对象的属性值
apollo-client-extend-binder 模块单独使用参考：apollo-client-extend-binder-demo

2、可配合 apollo-client-extend 基础模块使用，实现新增 Apollo Namespace 时，客户端的相应处理
使用可参考：apollo-client-extend-binder-demo
```

-   注意：
```textmate
1、如果binder的配置和管理配置同时修改的情况下，可能会导致管理配置还没有刷新到环境就先执行了binder监听器，
从而导致binder绑定失败
因为binder监听器和管理配置的监听器执行是没有顺序的，Apollo 是通过线程池提交的。
这种情况可以将配置的修改分为两步，不要同时修改。
```

-   实现：
```textmate
思路：
1、根据新增或删除的命名空间配置项，找出具体对应的 @ConfigurationProperties 标注的类集合
2、根据1的集合，更新对象
```

-   @ConfigurationProperties 总结
```textmate
@ConfigurationProperties 注解的几种用法：
1、在使用的地方标注：@EnableConfigurationProperties
这是标准用法，SpringBoot会自动注入@EnableConfigurationProperties 配置的类 MyProperties.class
@ConfigurationProperties(prefix = "my")
@Data
public class MyProperties {
    private String name;
}
@RestController
@EnableConfigurationProperties(MyProperties.class)
public class Controller {
    @Autowired
    private MyProperties myProperties;

    @GetMapping("/get")
    @ResponseBody
    public MyProperties get() {
        return myProperties;
    }
}
2、在配置类中标注：@EnableConfigurationProperties
这种用法跟第一种差不多
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(MyProperties.class)
public class MyConfig {
}
3、在启动类标注：@EnableConfigurationProperties
在配置类中声明，这种用法不推荐
@EnableConfigurationProperties
@SpringBootApplication
public class BootstrapApplication {
    public static void main(String[] args) {
        SpringApplication.run(BootstrapApplication.class, args);
    }
}
@Configuration(proxyBeanMethods = false)
public class MyConfig {
    @ConfigurationProperties(prefix = "my")
    @Bean
    public MyProperties myProperties() {
        return new MyProperties();
    }
}
```