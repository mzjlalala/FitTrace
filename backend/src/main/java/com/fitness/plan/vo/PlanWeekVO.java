package com.fitness.plan.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fitness.plan.entity.PlanWeek;
import lombok.Data;

import java.util.List;

/**
 * 计划周（含该周每日安排）
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PlanWeekVO {

    private Long id;
    private Integer weekNo;
    private List<PlanDayVO> days;

    public static PlanWeekVO of(PlanWeek week) {
        PlanWeekVO vo = new PlanWeekVO();
        vo.setId(week.getId());
        vo.setWeekNo(week.getWeekNo());
        return vo;
    }
}
