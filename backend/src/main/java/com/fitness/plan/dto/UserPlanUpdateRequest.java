package com.fitness.plan.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UserPlanUpdateRequest {

    @NotBlank(message = "状态不能为空")
    @Pattern(regexp = "^(COMPLETED|QUIT)$", message = "状态仅支持 COMPLETED / QUIT")
    private String status;
}
