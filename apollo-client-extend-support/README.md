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

-   Javassist 扩展方式
```
【使用】
增加启动JVM参数指定动态扩展：
-Dconfig.factory.proxy=true
默认打开; 设置false关闭
```

-   适配Apollo Client 版本：
v1.7.0

注意：
```
高于v1.7.0版本，现有的Javassist 扩展方式不一定支持，需要评估具体修改是否和Javassist的一致。
如果不支持，需要关闭Javassist扩展，手动实现扩展，可参考以下几个扩展点：
com.gittors.apollo.extend.support.ext.DefaultConfigExt
com.gittors.apollo.extend.support.ext.AbstractConfigExt
com.gittors.apollo.extend.support.ext.DefaultConfigFactoryExt

关闭Javassist扩展：
-Dconfig.factory.proxy=false
```