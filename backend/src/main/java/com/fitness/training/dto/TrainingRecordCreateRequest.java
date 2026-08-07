package com.fitness.training.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * 创建训练记录请求（记录 + 组数据一体提交）
 */
@Data
public class TrainingRecordCreateRequest {

    /** 训练日期（必填） */
    @NotNull(message = "训练日期不能为空")
    private LocalDate trainingDate;
    /** 训练时长（分钟） */
    private Integer durationMinutes;
    /** 训练感受（GOOD/NORMAL/TIRED） */
    @Pattern(regexp = "^(GOOD|NORMAL|TIRED)$", message = "训练感受只能是 GOOD/NORMAL/TIRED")
    private String feel;
    /** 备注 */
    @Size(max = 500, message = "备注不能超过 500 字")
    private String note;
    /** 关联计划 ID（可选） */
    private Long planId;
    /** 关联计划日 ID（可选） */
    private Long planDayId;
    /** 组数据（至少 1 组） */
    @NotEmpty(message = "组数据不能为空")
    @Valid
    private List<TrainingSetRequest> sets;
}
