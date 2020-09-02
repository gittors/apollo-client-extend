package com.gittors.apollo.extend.binder.exception;

/**
 * @author zlliu
 * @date 2020/9/2 18:09
 */
public class BinderException extends RuntimeException {
    public BinderException(String message) {
        super(message);
    }

    public BinderException(String message, Throwable cause) {
        super(message, cause);
    }
}
