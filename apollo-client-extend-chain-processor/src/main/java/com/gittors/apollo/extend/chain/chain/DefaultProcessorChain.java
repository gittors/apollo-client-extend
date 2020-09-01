package com.gittors.apollo.extend.chain.chain;

import com.gittors.apollo.extend.chain.context.Context;

/**
 * @author zlliu
 * @date 2020/8/14 22:36
 */
public class DefaultProcessorChain extends ProcessorChain {
    AbstractLinkedProcessor<?> first = new AbstractLinkedProcessor<Object>() {

        @Override
        public void entry(Context context, Object t, Object... args)
                throws Throwable {
            super.fireEntry(context, t, args);
        }
    };
    AbstractLinkedProcessor<?> end = first;

    @Override
    public void addFirst(AbstractLinkedProcessor<?> protocolProcessor) {
        protocolProcessor.setNext(first.getNext());
        first.setNext(protocolProcessor);
        if (end == first) {
            end = protocolProcessor;
        }
    }

    @Override
    public void addLast(AbstractLinkedProcessor<?> protocolProcessor) {
        end.setNext(protocolProcessor);
        end = protocolProcessor;
    }

    @Override
    public void setNext(AbstractLinkedProcessor<?> next) {
        addLast(next);
    }

    @Override
    public AbstractLinkedProcessor<?> getNext() {
        return first.getNext();
    }

    @Override
    public void entry(Context context, Object t, Object... args)
            throws Throwable {
        first.transformEntry(context, t, args);
    }

}
