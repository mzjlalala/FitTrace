package com.fitness.plan.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 计划模板
 */
@Data
@TableName("plan")
public class Plan {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    /** 适用目标（LOSE_FAT/MUSCLE_GAIN/KEEP_FIT/STRENGTH） */
    private String goal;
    /** 适用水平（BEGINNER/INTERMEDIATE/ADVANCED） */
    private String level;
    private Integer durationWeeks;
    private Integer frequencyPerWeek;
    private String description;
    private String coverImage;
    /** 状态（1=上架，0=下架） */
    private Integer status;
}
