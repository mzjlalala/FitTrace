package com.fitness.common.api;

import lombok.Getter;

/**
 * 统一返回体 { code, message, data }。
 * 业务错误返回 HTTP 200 + 业务 code；仅 Security 层未认证返回 HTTP 401。
 */
@Getter
public class R<T> {

    private final int code;
    private final String message;
    private final T data;

    private R(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> R<T> ok() {
        return ok(null);
    }

    public static <T> R<T> ok(T data) {
        return new R<>(ResultCode.OK.getCode(), ResultCode.OK.getMessage(), data);
    }

    public static <T> R<T> fail(ResultCode rc) {
        return new R<>(rc.getCode(), rc.getMessage(), null);
    }

    public static <T> R<T> fail(ResultCode rc, String message) {
        return new R<>(rc.getCode(), message, null);
    }
}
