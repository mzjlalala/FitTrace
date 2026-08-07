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

    /** 组数据 ID */
    private Long id;
    /** 动作 ID */
    private Long actionId;
    /** 动作名称 */
    private String actionName;
    /** 组序号（从 1 递增） */
    private Integer setNo;
    /** 重量（kg，徒手/有氧可为空） */
    private BigDecimal weightKg;
    /** 完成次数 */
    private Integer reps;
    /** 是否完成（TRUE=完成；PR 统计仅计入完成的组） */
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
