package com.fitness.plan.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserPlanStartRequest {

    @NotNull(message = "计划 ID 不能为空")
    private Long planId;
}
