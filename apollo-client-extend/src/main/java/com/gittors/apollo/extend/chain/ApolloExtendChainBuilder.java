package com.gittors.apollo.extend.chain;

import com.gittors.apollo.extend.chain.chain.DefaultProcessorChain;
import com.gittors.apollo.extend.chain.chain.ProcessorChain;
import com.gittors.apollo.extend.chain.spi.ChainBuilder;
import com.gittors.apollo.extend.common.spi.Ordered;

/**
 * @author zlliu
 * @date 2020/8/29 1:56
 */
public class ApolloExtendChainBuilder implements ChainBuilder {
    @Override
    public ProcessorChain build() {
        ProcessorChain chain = new DefaultProcessorChain();
        //  1、通过 Apollo 配置注入命名空间，此方式通过 Apollo application 命名空间为基点注册
        chain.addLast(new ApolloExtendNameSpaceConfigInjector());

        //  2、通过 Apollo 缓存文件注入命名空间，注意：此方式的管理配置无需放在 Apollo application 命名空间
        //  遗留问题：缓存文件对应的命名空间要纳入Apollo"运行时"管理，否则Apollo的配置不能同步到缓存文件
//        chain.addLast(new ApolloExtendNameSpaceFileInjector());
        return chain;
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
