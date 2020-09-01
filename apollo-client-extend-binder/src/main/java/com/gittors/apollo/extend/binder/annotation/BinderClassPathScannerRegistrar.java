package com.gittors.apollo.extend.binder.annotation;

import com.ctrip.framework.apollo.spring.util.BeanRegistrationUtil;
import com.gittors.apollo.extend.binder.processor.BinderPropertySourcesPostProcessor;
import com.gittors.apollo.extend.binder.scanner.BinderClassPathScanner;
import com.gittors.apollo.extend.common.constant.CommonBinderConstant;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.lang.annotation.Annotation;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * @author zlliu
 * @date 2020/8/19 21:39
 */
public class BinderClassPathScannerRegistrar implements ImportBeanDefinitionRegistrar, ResourceLoaderAware {

    private ResourceLoader resourceLoader;

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        BeanRegistrationUtil.registerBeanDefinitionIfNotExists(registry, BinderPropertySourcesPostProcessor.class.getName(),
                BinderPropertySourcesPostProcessor.class);

        AnnotationAttributes annAttrs =
                AnnotationAttributes.fromMap(importingClassMetadata.getAnnotationAttributes(BinderScan.class.getName()));
        BinderClassPathScanner scanner = new BinderClassPathScanner(registry);

        if (resourceLoader != null) {
            scanner.setResourceLoader(resourceLoader);
        }

        Class<? extends Annotation> annotationClass = annAttrs.getClass(CommonBinderConstant.BINDER_SCAN_ANNOTATION_CLASS);
        if (!Annotation.class.equals(annotationClass)) {
            scanner.setAnnotationClass(annotationClass);
        }

        Set<String> basePackages = new LinkedHashSet<>();
        for (String packageName : annAttrs.getStringArray(CommonBinderConstant.BINDER_SCAN_VALUE)) {
            if (StringUtils.hasText(packageName)) {
                basePackages.add(packageName);
            }
        }
        for (String packageName : annAttrs.getStringArray(CommonBinderConstant.BINDER_SCAN_BASE_PACKAGES)) {
            if (StringUtils.hasText(packageName)) {
                basePackages.add(packageName);
            }
        }
        for (Class<?> clazz : annAttrs.getClassArray(CommonBinderConstant.BINDER_SCAN_BASE_PACKAGE_CLASSES)) {
            basePackages.add(ClassUtils.getPackageName(clazz));
        }

        List<Class<?>> excludeClass = Lists.newArrayList();
        for (Class<?> clazz : annAttrs.getClassArray(CommonBinderConstant.BINDER_SCAN_EXCLUDE_CLASS)) {
            excludeClass.add(clazz);
        }
        scanner.setExcludeClass(excludeClass);

        scanner.registerFilters();
        scanner.scanner(StringUtils.toStringArray(basePackages));
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

}
