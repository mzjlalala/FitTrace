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

    /** 计划日 ID */
    private Long id;
    /** 日序号（从 1 开始） */
    private Integer dayNo;
    /** 是否休息日（TRUE=休息日无动作） */
    private Boolean restFlag;
    /** 当日主题（如"推日"/"全身A"） */
    private String title;
    /** 当日动作编排 */
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
