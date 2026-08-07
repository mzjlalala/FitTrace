package com.fitness.plan.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fitness.plan.entity.Plan;
import com.fitness.plan.entity.UserPlan;
import lombok.Data;

import java.time.LocalDate;

/**
 * 用户订阅记录（含计划概要）
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserPlanVO {

    /** 订阅记录 ID */
    private Long id;
    /** 计划 ID */
    private Long planId;
    /** 计划名称 */
    private String planName;
    /** 计划目标（LOSE_FAT/MUSCLE_GAIN/KEEP_FIT/STRENGTH） */
    private String planGoal;
    /** 计划水平（BEGINNER/INTERMEDIATE/ADVANCED） */
    private String planLevel;
    /** 开始日期 */
    private LocalDate startDate;
    /** 状态（ACTIVE=进行中/COMPLETED=已完成/QUIT=已退出） */
    private String status;

    public static UserPlanVO of(UserPlan up, Plan plan) {
        UserPlanVO vo = new UserPlanVO();
        vo.setId(up.getId());
        vo.setPlanId(up.getPlanId());
        vo.setStartDate(up.getStartDate());
        vo.setStatus(up.getStatus());
        if (plan != null) {
            vo.setPlanName(plan.getName());
            vo.setPlanGoal(plan.getGoal());
            vo.setPlanLevel(plan.getLevel());
        }
        return vo;
    }
}
