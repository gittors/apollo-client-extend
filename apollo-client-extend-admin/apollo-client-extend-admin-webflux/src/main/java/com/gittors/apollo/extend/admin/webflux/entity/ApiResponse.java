package com.gittors.apollo.extend.admin.webflux.entity;

import lombok.Builder;
import lombok.Getter;

/**
 * @author zlliu
 * @date 2020/8/5 17:23
 */
@Getter
@Builder
public class ApiResponse<T> {
    /**
     * 错误码
     */
    private int code;

    /**
     * 返回对象
     */
    private T data;

    /**
     * 错误信息
     */
    private String msg;

    public static ApiResponse success(Object object) {
        return success(object, "SUCCESS");
    }

    public static ApiResponse success(Object object, String msg) {
        return ApiResponse.builder()
                .msg(msg)
                .code(200)
                .data(object)
                .build();
    }

    public static ApiResponse fail() {
        return ApiResponse.builder()
                .code(400)
                .msg("BAD_REQUEST")
                .build();
    }

    public static ApiResponse fail(String msg) {
        return fail(400, msg);
    }

    public static ApiResponse fail(int code, String msg) {
        return ApiResponse.builder()
                .code(code)
                .msg(msg)
                .build();
    }
}