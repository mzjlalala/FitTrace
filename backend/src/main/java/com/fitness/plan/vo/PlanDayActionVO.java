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

    /** 编排 ID */
    private Long id;
    /** 当日执行顺序 */
    private Integer sort;
    /** 目标组数 */
    private Integer sets;
    /** 目标每组次数 */
    private Integer reps;
    /** 重量模式（FIXED=固定重量 / 递增） */
    private String weightMode;
    /** 组间休息（秒） */
    private Integer restSeconds;
    /** 动作概要（名称/肌群/难度/器械） */
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
