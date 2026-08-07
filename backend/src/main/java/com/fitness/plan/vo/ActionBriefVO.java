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

    private Long id;
    private String name;
    private String muscleGroup;
    private String difficulty;
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
