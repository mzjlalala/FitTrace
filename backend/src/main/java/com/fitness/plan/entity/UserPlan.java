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

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long planId;
    private LocalDate startDate;
    /** 状态（ACTIVE=进行中/COMPLETED=已完成/QUIT=已退出） */
    private String status;
}
