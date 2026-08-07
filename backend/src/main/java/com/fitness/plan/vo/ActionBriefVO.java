package com.fitness.plan.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fitness.action.entity.Action;
import lombok.Data;

/**
 * 计划详情中引用的动作概要信息
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ActionBriefVO {

    /** 动作 ID */
    private Long id;
    /** 动作名称 */
    private String name;
    /** 目标肌群 */
    private String muscleGroup;
    /** 难度（BEGINNER/INTERMEDIATE/ADVANCED） */
    private String difficulty;
    /** 所需器械 */
    private String equipment;

    public static ActionBriefVO of(Action action) {
        ActionBriefVO vo = new ActionBriefVO();
        vo.setId(action.getId());
        vo.setName(action.getName());
        vo.setMuscleGroup(action.getMuscleGroup());
        vo.setDifficulty(action.getDifficulty());
        vo.setEquipment(action.getEquipment());
        return vo;
    }
}
