package com.fitness.plan.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fitness.plan.entity.PlanDay;
import lombok.Data;

import java.util.List;

/**
 * 计划日（含当日动作编排）
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PlanDayVO {

    private Long id;
    private Integer dayNo;
    private Boolean restFlag;
    private String title;
    private List<PlanDayActionVO> actions;

    public static PlanDayVO of(PlanDay day) {
        PlanDayVO vo = new PlanDayVO();
        vo.setId(day.getId());
        vo.setDayNo(day.getDayNo());
        vo.setRestFlag(day.getRestFlag());
        vo.setTitle(day.getTitle());
        return vo;
    }
}
