package com.gittors.apollo.extend.binder.annotation;

import com.gittors.apollo.extend.binder.context.BinderContext;
import com.gittors.apollo.extend.binder.spi.BinderAnnotationProcessor;
import com.gittors.apollo.extend.common.constant.CommonBinderConstant;
import com.gittors.apollo.extend.common.service.ServiceLookUp;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.TypeFilter;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author zlliu
 * @date 2020/8/10 21:39
 */
@Slf4j
public class BinderClassPathScanner extends ClassPathBeanDefinitionScanner {

    private final BinderAnnotationProcessor<Map<String, Class<?>>> binderAnnotationProcessor =
            ServiceLookUp.loadPrimary(BinderAnnotationProcessor.class);

    private ResourcePatternResolver resourcePatternResolver;

    /**
     * 包涵的类型
     */
    private final List<TypeFilter> includeFilters = new LinkedList<>();

    /**
     * 排除的类型
     */
    private final List<TypeFilter> excludeFilters = new LinkedList<>();

    /**
     * 指定排除的class
     */
    private List<Class<?>> excludeClass = new LinkedList<>();

    /**
     * 注解类型
     */
    private Class<? extends Annotation> annotationClass;

    public BinderClassPathScanner(BeanDefinitionRegistry registry) {
        super(registry, false);
    }

    public void registerFilters() {
        if (this.annotationClass != null) {
            addIncludeFilter(new AnnotationTypeFilter(this.annotationClass));
        }

        // exclude package-info.java and other class
        addExcludeFilter((metadataReader, metadataReaderFactory) -> {
            String className = metadataReader.getClassMetadata().getClassName();
            for (Class<?> clazz : excludeClass) {
                if (className.endsWith(clazz.getSimpleName())) {
                    return true;
                }
            }
            return className.endsWith(CommonBinderConstant.PACKAGE_INFO_CLASS_SUFFIX);
        });
    }

    public void scanner(String... basePackages) {
        Map<String, Class<?>> candidates = Maps.newLinkedHashMap();
        for (String basePackage : basePackages) {
            candidates.putAll(doScanner(basePackage));
        }
        BinderContext.INSTANCE.initBinderMap(candidates);
    }

    /**
     * 扫描指定包，
     * 包涵 @ConfigurationProperties 注解，
     * 且满足指定条件的类
     *
     * @param basePackage   扫描包路径
     * @return
     */
    private Map<String, Class<?>> doScanner(String basePackage) {
        Map<String, Class<?>> candidates = new LinkedHashMap<>();
        try {
            String packageSearchPath = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX +
                    resolveBasePackage(basePackage) + '/' + CommonBinderConstant.DEFAULT_RESOURCE_PATTERN;

            Resource[] resources = getResourcePatternResolver().getResources(packageSearchPath);
            boolean traceEnabled = log.isTraceEnabled();
            for (Resource resource : resources) {
                if (resource.isReadable()) {
                    try {
                        MetadataReader metadataReader = getMetadataReaderFactory().getMetadataReader(resource);
                        if (isCandidate(metadataReader)) {
                            binderAnnotationProcessor.process(candidates, metadataReader, annotationClass.getName());
                        }
                        else {
                            if (traceEnabled) {
                                log.trace("Ignored because not matching any filter: " + resource);
                            }
                        }
                    }
                    catch (Throwable ex) {
                        throw new BeanDefinitionStoreException(
                                "Failed to read candidate component class: " + resource, ex);
                    }
                }
                else {
                    if (traceEnabled) {
                        log.trace("Ignored because not readable: " + resource);
                    }
                }
            }
        }
        catch (IOException ex) {
            throw new BeanDefinitionStoreException("I/O failure during classpath scanning", ex);
        }
        return candidates;
    }

    /**
     * Add an include type filter to the <i>end</i> of the inclusion list.
     */
    @Override
    public void addIncludeFilter(TypeFilter includeFilter) {
        this.includeFilters.add(includeFilter);
    }

    /**
     * Add an exclude type filter to the <i>front</i> of the exclusion list.
     */
    @Override
    public void addExcludeFilter(TypeFilter excludeFilter) {
        this.excludeFilters.add(0, excludeFilter);
    }

    protected boolean isCandidate(MetadataReader metadataReader) throws IOException {
        for (TypeFilter tf : this.excludeFilters) {
            if (tf.match(metadataReader, getMetadataReaderFactory())) {
                return false;
            }
        }
        for (TypeFilter tf : this.includeFilters) {
            if (tf.match(metadataReader, getMetadataReaderFactory())) {
                return true;
            }
        }
        return false;
    }

    private ResourcePatternResolver getResourcePatternResolver() {
        if (this.resourcePatternResolver == null) {
            this.resourcePatternResolver = new PathMatchingResourcePatternResolver();
        }
        return this.resourcePatternResolver;
    }

    public void setAnnotationClass(Class<? extends Annotation> annotationClass) {
        this.annotationClass = annotationClass;
    }

    public void setExcludeClass(List<Class<?>> excludeClass) {
        this.excludeClass = excludeClass;
    }
}
