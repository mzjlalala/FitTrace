package com.fitness.plan.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fitness.plan.entity.Plan;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PlanVO {

    private Long id;
    private String name;
    private String goal;
    private String level;
    private Integer durationWeeks;
    private Integer frequencyPerWeek;
    private String description;
    private String coverImage;

    public static PlanVO of(Plan plan) {
        PlanVO vo = new PlanVO();
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
