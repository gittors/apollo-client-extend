package com.gittors.apollo.extend.chain.chain;

import com.gittors.apollo.extend.chain.context.Context;

import java.util.Map;

/**
 * @author zlliu
 * @date 2020/8/14 22:31
 */
public abstract class AbstractLinkedProcessor<T> implements Processor<T> {
    private AbstractLinkedProcessor<?> next = null;

    @Override
    public void fireEntry(Context context, Object obj, Map<String, Object> args)
            throws Throwable {
        if (next != null) {
            next.transformEntry(context, obj, args);
        }
    }

    void transformEntry(Context context, Object object, Map<String, Object> args)
            throws Throwable {
        T t = (T) object;
        entry(context, t, args);
    }

    public AbstractLinkedProcessor<?> getNext() {
        return next;
    }

    public void setNext(AbstractLinkedProcessor<?> next) {
        this.next = next;
    }
}
