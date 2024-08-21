package com.gittors.apollo.extend.common.service;

/**
 * @author zlliu
 * @date 2022/03/28 12:13
 */
public interface Injector {

  /**
   * 根据对象class类型返回对象实例
   */
  <T> T getInstance(Class<T> clazz);

  /**
   * 根据对象class类型+名称返回对象实例
   */
  <T> T getInstance(Class<T> clazz, String name);
}