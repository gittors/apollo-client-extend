package com.gittors.apollo.extend.binder.spi;

import com.gittors.apollo.extend.common.constant.CommonBinderConstant;
import com.gittors.apollo.extend.common.spi.Ordered;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.util.ClassUtils;

import java.util.Map;

/**
 * @author zlliu
 * @date 2020/8/30 17:43
 */
public class DefaultBinderAnnotationProcessor implements BinderAnnotationProcessor<Map<String, Class<?>>> {
    @Override
    public void process(Map<String, Class<?>> request, Object... objects) {
        MetadataReader metadataReader = (MetadataReader) objects[0];
        AnnotationAttributes annAttrs = AnnotationAttributes.fromMap(metadataReader.getAnnotationMetadata()
                .getAnnotationAttributes((String) objects[1]));

        request.put(annAttrs.getString(CommonBinderConstant.CONFIGURATION_PROPERTIES_PREFIX_KEY),
                ClassUtils.resolveClassName(metadataReader.getClassMetadata().getClassName(), null));
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
