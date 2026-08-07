package com.fitness.plan.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 计划周
 */
@Data
@TableName("plan_week")
public class PlanWeek {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long planId;
    /** 第几周（从 1 开始） */
    private Integer weekNo;
}
