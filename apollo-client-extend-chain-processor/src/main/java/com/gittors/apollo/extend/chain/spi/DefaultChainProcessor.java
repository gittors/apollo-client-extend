package com.gittors.apollo.extend.chain.spi;

import com.gittors.apollo.extend.common.spi.Ordered;

/**
 * @author zlliu
 * @date 2020/8/15 13:13
 */
public class DefaultChainProcessor extends ChainProcessor.AbstractChainProcessor {

    @Override
    public Object process(Object request, String name, Object... objects) throws Throwable {
        return super.process(request, name, objects);
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
