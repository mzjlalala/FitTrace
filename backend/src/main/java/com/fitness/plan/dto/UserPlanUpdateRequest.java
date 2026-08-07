package com.fitness.plan.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 更新订阅状态请求
 */
@Data
public class UserPlanUpdateRequest {

    /** 目标状态（COMPLETED=已完成 / QUIT=已退出） */
    @NotBlank(message = "状态不能为空")
    @Pattern(regexp = "^(COMPLETED|QUIT)$", message = "状态仅支持 COMPLETED / QUIT")
    private String status;
}
