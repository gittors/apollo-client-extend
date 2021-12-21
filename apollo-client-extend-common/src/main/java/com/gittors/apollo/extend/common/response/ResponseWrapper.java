package com.gittors.apollo.extend.common.response;

/**
 * @author zlliu
 * @date 2020/8/19 20:06
 */
public class ResponseWrapper<T> {
    /**
     * 是否成功
     */
    private boolean success;

    /**
     * 错误码
     */
    private Integer code;

    /**
     * 错误信息
     */
    private String msg;

    /**
     * 返回对象
     */
    private T data;

    public ResponseWrapper() {
    }

    public ResponseWrapper(Integer code) {
        this(code, (String)null);
    }

    public ResponseWrapper(String msg) {
        this.msg = msg;
    }

    public ResponseWrapper(Integer code, String msg) {
        this(code, msg, null);
    }

    public ResponseWrapper(Integer code, String msg, T data) {
        this(false, code, msg, data);
    }

    public ResponseWrapper(boolean success, Integer code, String msg, T data) {
        this.success = success;
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public static ResponseWrapper success() {
        return success("success");
    }

    public static ResponseWrapper success(Object data) {
        return success("success", data);
    }

    public static ResponseWrapper success(String msg) {
        return success(msg, (Object)null);
    }

    public static ResponseWrapper success(String msg, Object data) {
        return success(200, msg, data);
    }

    public static ResponseWrapper success(Integer code, String msg, Object data) {
        return new ResponseWrapper(true, code, msg, data);
    }

    public static ResponseWrapper fail() {
        return fail("failed");
    }

    public static ResponseWrapper fail(Object data) {
        return fail("failed", data);
    }

    public static ResponseWrapper fail(String msg) {
        return fail(msg, (Object)null);
    }

    public static ResponseWrapper fail(String msg, Object data) {
        return fail(400, msg, data);
    }

    public static ResponseWrapper fail(Integer code, String msg, Object data) {
        return new ResponseWrapper(false, code, msg, data);
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
