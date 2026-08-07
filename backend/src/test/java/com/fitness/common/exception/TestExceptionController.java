package com.fitness.common.exception;

import com.fitness.common.api.Response;
import com.fitness.common.api.ResultCode;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestExceptionController {

    @GetMapping("/test/biz-ex")
    public Response<Void> biz() {
        throw new BizException(ResultCode.CONFLICT, "冲突了");
    }

    @GetMapping("/test/illegal")
    public Response<Void> illegal() {
        throw new IllegalArgumentException("boom");
    }
}
