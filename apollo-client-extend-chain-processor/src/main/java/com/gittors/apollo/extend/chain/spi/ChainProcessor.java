package com.gittors.apollo.extend.chain.spi;

import com.gittors.apollo.extend.chain.chain.ChainProvider;
import com.gittors.apollo.extend.chain.chain.Processor;
import com.gittors.apollo.extend.chain.context.Context;
import com.gittors.apollo.extend.common.spi.Ordered;

/**
 * @author zlliu
 * @date 2020/8/15 13:11
 */
public interface ChainProcessor<I, O> extends Ordered {
    /**
     * 链式处理
     * @param request   请求参数
     * @param name  请求名称
     * @param objects   其他参数
     * @return
     * @throws Throwable
     */
    O process(I request, String name, Object... objects) throws Throwable;

    abstract class AbstractChainProcessor<I, O> implements ChainProcessor<I, O> {
        @Override
        public O process(I request, String name, Object... objects) throws Throwable {
            entry(request, name, objects);
            return null;
        }

        protected void entry(I request, String name, Object... objects) throws Throwable {
            Context context = new Context(name);
            Processor<Object> chain = ChainProvider.newChain();
            chain.entry(context, request, objects);
        }
    }
}
