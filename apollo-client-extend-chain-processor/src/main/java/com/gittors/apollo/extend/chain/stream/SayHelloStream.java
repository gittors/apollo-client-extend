package com.gittors.apollo.extend.chain.stream;

import com.gittors.apollo.extend.chain.chain.AbstractLinkedProcessor;
import com.gittors.apollo.extend.chain.context.Context;

/**
 * @author zlliu
 * @date 2020/8/14 22:41
 */
public class SayHelloStream extends AbstractLinkedProcessor<Object> {
    @Override
    public void entry(Context context, Object param, Object... args) throws Throwable {
        System.out.println("----------- say hello ------------");

        //  enter into next stream
        fireEntry(context, param, args);
    }

}
