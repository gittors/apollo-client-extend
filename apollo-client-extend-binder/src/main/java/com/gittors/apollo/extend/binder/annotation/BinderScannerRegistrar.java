package com.gittors.apollo.extend.binder.annotation;

/**
 * 适配 {@link BinderScan 注解}
 *
 * @author zlliu
 * @date 2020/8/19 21:39
 */
public class BinderScannerRegistrar extends AbstractBinderRegistrar {

    @Override
    protected String getAnnotationName() {
        return BinderScan.class.getName();
    }
}
