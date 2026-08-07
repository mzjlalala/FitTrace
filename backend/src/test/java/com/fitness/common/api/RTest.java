package com.fitness.common.api;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RTest {

    @Test
    void ok_createsSuccessResponse() {
        R<String> r = R.ok("hello");
        assertThat(r.getCode()).isEqualTo(200);
        assertThat(r.getMessage()).isEqualTo("操作成功");
        assertThat(r.getData()).isEqualTo("hello");
    }

    @Test
    void fail_createsErrorResponse() {
        R<Void> r = R.fail(ResultCode.UNAUTHORIZED);
        assertThat(r.getCode()).isEqualTo(401);
        assertThat(r.getData()).isNull();
    }

    @Test
    void fail_withCustomMessage_overridesDefault() {
        R<Void> r = R.fail(ResultCode.CONFLICT, "用户名已被占用");
        assertThat(r.getCode()).isEqualTo(409);
        assertThat(r.getMessage()).isEqualTo("用户名已被占用");
    }
}
