package com.fitness.plan.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 计划日
 */
@Data
@TableName("plan_day")
public class PlanDay {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long planWeekId;
    /** 第几天（从 1 开始） */
    private Integer dayNo;
    /** 是否休息日 */
    private Boolean restFlag;
    /** 训练日标题（如：推日/拉日） */
    private String title;
}
