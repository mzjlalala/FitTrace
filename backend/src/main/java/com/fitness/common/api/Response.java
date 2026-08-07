package com.fitness.common.api;

import lombok.Getter;

/**
 * 统一返回体 { code, message, data }。
 * 业务错误返回 HTTP 200 + 业务 code；仅 Security 层未认证返回 HTTP 401。
 */
@Getter
public class Response<T> {

    private final int code;
    private final String message;
    private final T data;

    private Response(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> Response<T> ok() {
        return ok(null);
    }

    public static <T> Response<T> ok(T data) {
        return new Response<>(ResultCode.OK.getCode(), ResultCode.OK.getMessage(), data);
    }

    public static <T> Response<T> fail(ResultCode rc) {
        return new Response<>(rc.getCode(), rc.getMessage(), null);
    }

    public static <T> Response<T> fail(ResultCode rc, String message) {
        return new Response<>(rc.getCode(), message, null);
    }
}
