package com.fitness.training.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fitness.training.entity.TrainingRecordSet;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 训练组数据（含动作名称）
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TrainingSetVO {

    private Long id;
    private Long actionId;
    private String actionName;
    private Integer setNo;
    private BigDecimal weightKg;
    private Integer reps;
    private Boolean doneFlag;

    public static TrainingSetVO of(TrainingRecordSet set, String actionName) {
        TrainingSetVO vo = new TrainingSetVO();
        vo.setId(set.getId());
        vo.setActionId(set.getActionId());
        vo.setActionName(actionName);
        vo.setSetNo(set.getSetNo());
        vo.setWeightKg(set.getWeightKg());
        vo.setReps(set.getReps());
        vo.setDoneFlag(set.getDoneFlag());
        return vo;
    }
}
