# Apollo Client Extend Support
```
现有的*Ext扩展接口都是静态硬编码方式，缺点也很明显：
Apollo Client接口如果修改，那么*Ext扩展接口也要相应修改(具体由依赖Apollo Client的版本差异决定)。
```

-   待解决问题：将*Ext扩展类的静态扩展方式(硬编码)转为通过 Javassist 方式
```
【思路】
1、通过 Javassist 构建 *Ext扩展类对象
2、然后注入到 DefaultInjectorExt 中
```

-   已解决问题【待解决问题已解决】
```
【使用】
增加启动JVM参数指定动态扩展：
-Dconfig.factory.proxy
默认打开; false关闭
```

-   适配Apollo Client 版本：
v1.7.0