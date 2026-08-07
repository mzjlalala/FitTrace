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

    private Long id;
    private Long planId;
    private String planName;
    private String planGoal;
    private String planLevel;
    private LocalDate startDate;
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
