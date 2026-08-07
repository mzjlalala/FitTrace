package com.fitness.admin.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fitness.plan.entity.Plan;
import lombok.Data;

/**
 * 管理后台：计划视图（含状态，供管理列表/编辑回显）
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AdminPlanVO {

    private Long id;
    private String name;
    private String goal;
    private String level;
    private Integer durationWeeks;
    private Integer frequencyPerWeek;
    private String description;
    private Integer status;

    public static AdminPlanVO of(Plan plan) {
        AdminPlanVO vo = new AdminPlanVO();
        vo.setId(plan.getId());
        vo.setName(plan.getName());
        vo.setGoal(plan.getGoal());
        vo.setLevel(plan.getLevel());
        vo.setDurationWeeks(plan.getDurationWeeks());
        vo.setFrequencyPerWeek(plan.getFrequencyPerWeek());
        vo.setDescription(plan.getDescription());
        vo.setStatus(plan.getStatus());
        return vo;
    }
}
