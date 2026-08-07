package com.fitness.common.exception;

import com.fitness.common.api.Response;
import com.fitness.common.api.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BizException.class)
    public Response<Void> handleBizException(BizException e) {
        return Response.fail(e.getResultCode(), e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Response<Void> handleValidation(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        return Response.fail(ResultCode.BAD_REQUEST, msg);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Response<Void> handleUnreadable(HttpMessageNotReadableException e) {
        return Response.fail(ResultCode.BAD_REQUEST, "请求体格式错误");
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public Response<Void> handleNotFound(NoResourceFoundException e) {
        return Response.fail(ResultCode.NOT_FOUND);
    }

    @ExceptionHandler(Exception.class)
    public Response<Void> handleException(Exception e) {
        log.error("Unhandled exception", e);
        return Response.fail(ResultCode.INTERNAL_ERROR);
    }
}
