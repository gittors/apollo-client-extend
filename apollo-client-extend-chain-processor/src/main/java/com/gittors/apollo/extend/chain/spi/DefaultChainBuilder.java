package com.gittors.apollo.extend.chain.spi;

import com.gittors.apollo.extend.chain.chain.DefaultProcessorChain;
import com.gittors.apollo.extend.chain.chain.ProcessorChain;
import com.gittors.apollo.extend.chain.stream.SayHelloStream;
import com.gittors.apollo.extend.common.spi.Ordered;

/**
 * @author zlliu
 * @date 2020/8/14 22:35
 */
public class DefaultChainBuilder implements ChainBuilder {

    @Override
    public ProcessorChain build() {
        ProcessorChain chain = new DefaultProcessorChain();
        //  add say hello stream node
        chain.addLast(new SayHelloStream());
        //  add other stream node
        //  ...
        return chain;
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
