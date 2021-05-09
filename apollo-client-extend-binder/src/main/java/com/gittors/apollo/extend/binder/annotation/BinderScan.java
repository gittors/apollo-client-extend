package com.gittors.apollo.extend.binder.annotation;

import com.gittors.apollo.extend.common.constant.CommonBinderConstant;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Import;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author zlliu
 * @date 2020/8/19 21:39
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import(BinderScannerRegistrar.class)
public @interface BinderScan {

    /**
     * Scan the package path
     * @return base packages to scan
     */
    String[] value() default {};

    /**
     * Scan the package path
     * @return base packages to scan
     */
    String[] basePackages() default CommonBinderConstant.BINDER_SCAN_BASE_PACKAGE;

    /**
     * Scan, according to the class path
     * @return
     */
    Class<?>[] basePackageClasses() default {};

    /**
     * exclude specified class
     * @return
     */
    Class<?>[] excludeClass() default {};

    /**
     * Scan annotation
     * @return
     */
    Class<? extends Annotation> annotationClass() default ConfigurationProperties.class;

}