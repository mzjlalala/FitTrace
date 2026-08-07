package com.fitness.common.api;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ResponseTest {

    @Test
    void ok_createsSuccessResponse() {
        Response<String> r = Response.ok("hello");
        assertThat(r.getCode()).isEqualTo(200);
        assertThat(r.getMessage()).isEqualTo("操作成功");
        assertThat(r.getData()).isEqualTo("hello");
    }

    @Test
    void fail_createsErrorResponse() {
        Response<Void> r = Response.fail(ResultCode.UNAUTHORIZED);
        assertThat(r.getCode()).isEqualTo(401);
        assertThat(r.getData()).isNull();
    }

    @Test
    void fail_withCustomMessage_overridesDefault() {
        Response<Void> r = Response.fail(ResultCode.CONFLICT, "用户名已被占用");
        assertThat(r.getCode()).isEqualTo(409);
        assertThat(r.getMessage()).isEqualTo("用户名已被占用");
    }
}
