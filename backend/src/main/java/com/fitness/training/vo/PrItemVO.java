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

    private Long actionId;
    private String actionName;
    private BigDecimal weightKg;
    private Integer reps;
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
