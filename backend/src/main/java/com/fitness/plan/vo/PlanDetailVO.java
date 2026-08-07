package com.fitness.plan.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fitness.plan.entity.Plan;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 计划详情（列表字段 + 周/日/动作树）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PlanDetailVO extends PlanVO {

    /** 周安排（MVP 为单周循环，长度 1） */
    private List<PlanWeekVO> weeks;

    public static PlanDetailVO of(Plan plan) {
        PlanDetailVO vo = new PlanDetailVO();
        vo.setId(plan.getId());
        vo.setName(plan.getName());
        vo.setGoal(plan.getGoal());
        vo.setLevel(plan.getLevel());
        vo.setDurationWeeks(plan.getDurationWeeks());
        vo.setFrequencyPerWeek(plan.getFrequencyPerWeek());
        vo.setDescription(plan.getDescription());
        vo.setCoverImage(plan.getCoverImage());
        return vo;
    }
}
