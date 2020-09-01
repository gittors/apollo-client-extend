package com.gittors.apollo.extend.support.ext;

/**
 * @author zlliu
 * @date 2020/7/27 15:43
 */
public interface PropertiesCallBack<T> {
    /**
     * 配置回调
     *
     * @param propertyFile
     * @return
     */
    T callBack(T propertyFile);
}
