package com.fitness.training.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 训练记录（一次完整训练）
 */
@Data
@TableName("training_record")
public class TrainingRecord {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 用户 ID */
    private Long userId;
    /** 计划 ID（可选，自由训练可为空） */
    private Long planId;
    /** 计划日 ID（可选） */
    private Long planDayId;
    /** 训练日期 */
    private LocalDate trainingDate;
    /** 训练时长（分钟） */
    private Integer durationMinutes;
    /** 训练感受（GOOD=状态好/NORMAL=一般/TIRED=疲劳） */
    private String feel;
    /** 备注 */
    private String note;
    /** 创建时间（自动填充） */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    /** 更新时间（自动填充） */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
