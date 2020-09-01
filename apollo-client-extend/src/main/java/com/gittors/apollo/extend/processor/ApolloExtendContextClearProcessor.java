package com.gittors.apollo.extend.processor;

import com.gittors.apollo.extend.context.ApolloExtendContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;

/**
 * @author zlliu
 * @date 2020/7/25 11:18
 */
@Slf4j
public class ApolloExtendContextClearProcessor implements DisposableBean {

    @Override
    public void destroy() throws Exception {
        log.info("ContextClearDisposable#destroy...");
        ApolloExtendContext.INSTANCE.getSpringValueMap().clear();
    }
}
