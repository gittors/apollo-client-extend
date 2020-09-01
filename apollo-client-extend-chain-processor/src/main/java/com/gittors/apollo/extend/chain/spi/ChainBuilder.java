package com.gittors.apollo.extend.chain.spi;

import com.gittors.apollo.extend.chain.chain.ProcessorChain;
import com.gittors.apollo.extend.common.spi.Ordered;

/**
 * @author zlliu
 * @date 2020/8/14 22:35
 */
public interface ChainBuilder extends Ordered {

    /**
     * Build the processor chain.
     *
     * @return
     */
    ProcessorChain build();
}
