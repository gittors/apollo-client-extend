package com.gittors.apollo.extend.chain.spi;

import com.gittors.apollo.extend.common.service.Ordered;

/**
 * @author zlliu
 * @date 2020/8/15 13:13
 */
public class DefaultChainProcessor extends ChainProcessor.AbstractChainProcessor {

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
