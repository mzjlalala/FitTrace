package com.fitness.plan.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 当日动作编排
 */
@Data
@TableName("plan_day_action")
public class PlanDayAction {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 所属计划日 ID */
    private Long planDayId;
    /** 动作 ID */
    private Long actionId;
    /** 动作顺序（越小越靠前） */
    private Integer sort;
    /** 建议组数 */
    private Integer sets;
    /** 每组建议次数 */
    private Integer reps;
    /** 重量模式（FIXED=固定重量/递增等） */
    private String weightMode;
    /** 组间休息秒数 */
    private Integer restSeconds;
}
