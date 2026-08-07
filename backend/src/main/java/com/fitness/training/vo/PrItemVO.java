package com.fitness.training.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fitness.training.mapper.TrainingRecordSetMapper;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 个人纪录（PR）：某动作有史以来的最佳组
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PrItemVO {

    /** 动作 ID */
    private Long actionId;
    /** 动作名称 */
    private String actionName;
    /** 最佳组重量（kg） */
    private BigDecimal weightKg;
    /** 最佳组完成次数 */
    private Integer reps;
    /** 创造纪录的日期 */
    private LocalDate recordDate;

    public static PrItemVO of(TrainingRecordSetMapper.PrRow row, String actionName) {
        PrItemVO vo = new PrItemVO();
        vo.setActionId(row.getActionId());
        vo.setActionName(actionName);
        vo.setWeightKg(row.getWeightKg());
        vo.setReps(row.getReps());
        vo.setRecordDate(row.getRecordDate());
        return vo;
    }
}
