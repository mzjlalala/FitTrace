package com.fitness.plan.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fitness.plan.entity.Plan;
import lombok.Data;

/**
 * 计划概要
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PlanVO {

    /** 计划 ID */
    private Long id;
    /** 计划名称 */
    private String name;
    /** 训练目标（LOSE_FAT/MUSCLE_GAIN/KEEP_FIT/STRENGTH） */
    private String goal;
    /** 难度水平（BEGINNER/INTERMEDIATE/ADVANCED） */
    private String level;
    /** 计划总周数 */
    private Integer durationWeeks;
    /** 每周训练频次 */
    private Integer frequencyPerWeek;
    /** 计划描述 */
    private String description;
    /** 封面图 URL */
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
