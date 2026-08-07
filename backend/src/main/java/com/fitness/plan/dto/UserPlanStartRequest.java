package com.fitness.plan.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 开始（订阅）计划请求
 */
@Data
public class UserPlanStartRequest {

    /** 计划 ID（必填） */
    @NotNull(message = "计划 ID 不能为空")
    private Long planId;
}
