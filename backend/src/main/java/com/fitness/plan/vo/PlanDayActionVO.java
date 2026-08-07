package com.fitness.plan.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fitness.plan.entity.PlanDayAction;
import lombok.Data;

/**
 * 当日动作编排（含动作概要）
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PlanDayActionVO {

    private Long id;
    private Integer sort;
    private Integer sets;
    private Integer reps;
    private String weightMode;
    private Integer restSeconds;
    private ActionBriefVO action;

    public static PlanDayActionVO of(PlanDayAction pda, ActionBriefVO action) {
        PlanDayActionVO vo = new PlanDayActionVO();
        vo.setId(pda.getId());
        vo.setSort(pda.getSort());
        vo.setSets(pda.getSets());
        vo.setReps(pda.getReps());
        vo.setWeightMode(pda.getWeightMode());
        vo.setRestSeconds(pda.getRestSeconds());
        vo.setAction(action);
        return vo;
    }
}
