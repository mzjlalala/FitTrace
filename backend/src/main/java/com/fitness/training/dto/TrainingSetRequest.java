package com.fitness.training.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 训练组数据请求（创建/更新训练记录时提交的一组动作）
 */
@Data
public class TrainingSetRequest {

    /** 动作 ID（必填） */
    @NotNull(message = "动作 ID 不能为空")
    private Long actionId;
    /** 重量（kg，徒手/有氧可为空） */
    private BigDecimal weightKg;
    /** 完成次数 */
    private Integer reps;
    /** 是否完成（默认 true；PR 统计仅计入完成的组） */
    private Boolean doneFlag;
}
