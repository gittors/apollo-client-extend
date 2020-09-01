package com.gittors.apollo.extend.common.response;

/**
 * @author zlliu
 * @date 2020/8/19 20:06
 */
public class BaseResponse<T> {
    private boolean success;
    private Integer code;
    private String msg;
    private T data;

    public BaseResponse() {
    }

    public BaseResponse(Integer code) {
        this(code, (String)null);
    }

    public BaseResponse(String msg) {
        this.msg = msg;
    }

    public BaseResponse(Integer code, String msg) {
        this(code, msg, null);
    }

    public BaseResponse(Integer code, String msg, T data) {
        this(false, code, msg, data);
    }

    public BaseResponse(boolean success, Integer code, String msg, T data) {
        this.success = success;
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public static BaseResponse success() {
        return success("success");
    }

    public static BaseResponse success(Object data) {
        return success("success", data);
    }

    public static BaseResponse success(String msg) {
        return success(msg, (Object)null);
    }

    public static BaseResponse success(String msg, Object data) {
        return success(200, msg, data);
    }

    public static BaseResponse success(Integer code, String msg, Object data) {
        return new BaseResponse(true, code, msg, data);
    }

    public static BaseResponse fail() {
        return fail("failed");
    }

    public static BaseResponse fail(Object data) {
        return fail("failed", data);
    }

    public static BaseResponse fail(String msg) {
        return fail(msg, (Object)null);
    }

    public static BaseResponse fail(String msg, Object data) {
        return fail(400, msg, data);
    }

    public static BaseResponse fail(Integer code, String msg, Object data) {
        return new BaseResponse(false, code, msg, data);
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public void setData(T data) {
        this.data = data;
    }

    public boolean isSuccess() {
        return this.success;
    }

    public Integer getCode() {
        return this.code;
    }

    public String getMsg() {
        return this.msg;
    }

    public T getData() {
        return this.data;
    }

    public String toString() {
        return "BaseResponse(success=" + this.isSuccess() + ", code=" + this.getCode() + ", msg=" + this.getMsg() + ", data=" + this.getData() + ")";
    }
}
