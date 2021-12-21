package com.gittors.apollo.extend.chain.spi;

import com.gittors.apollo.extend.chain.chain.AbstractLinkedProcessor;
import com.gittors.apollo.extend.chain.chain.DefaultProcessorChain;
import com.gittors.apollo.extend.chain.chain.ProcessorChain;
import com.gittors.apollo.extend.chain.context.Context;
import com.gittors.apollo.extend.common.spi.Ordered;

import java.util.Map;

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

    class SayHelloStream extends AbstractLinkedProcessor<Object> {
        @Override
        public void entry(Context context, Object param, Map<String, Object> args) throws Throwable {
            System.out.println("----------- say hello ------------");

            //  enter into next stream
            fireEntry(context, param, args);
        }

    }

    @Override
    public int getOrder() {
        //  最低优先级
        return Ordered.LOWEST_PRECEDENCE;
    }
}
