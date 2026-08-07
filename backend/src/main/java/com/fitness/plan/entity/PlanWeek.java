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

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 所属计划 ID */
    private Long planId;
    /** 第几周（从 1 开始） */
    private Integer weekNo;
}
