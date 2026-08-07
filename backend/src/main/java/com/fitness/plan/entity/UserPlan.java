package com.fitness.plan.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;

/**
 * 用户订阅的计划
 */
@Data
@TableName("user_plan")
public class UserPlan {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 用户 ID */
    private Long userId;
    /** 计划 ID */
    private Long planId;
    /** 开始日期 */
    private LocalDate startDate;
    /** 状态（ACTIVE=进行中/COMPLETED=已完成/QUIT=已退出） */
    private String status;
}
