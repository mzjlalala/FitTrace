package com.fitness.system.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户身体数据与训练目标
 */
@Data
@TableName("user_profile")
public class UserProfile {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 用户 ID */
    private Long userId;
    /** 性别（MALE/FEMALE） */
    private String gender;
    /** 出生日期 */
    private LocalDate birthDate;
    /** 身高（cm） */
    private BigDecimal heightCm;
    /** 体重（kg） */
    private BigDecimal weightKg;
    /** 训练目标（LOSE_FAT/MUSCLE_GAIN/KEEP_FIT/STRENGTH） */
    private String goal;
    /** 健身水平（BEGINNER/INTERMEDIATE/ADVANCED） */
    private String fitnessLevel;
    /** 每周训练频次 */
    private Integer weeklyFrequency;
    /** 创建时间（自动填充） */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    /** 更新时间（自动填充） */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
