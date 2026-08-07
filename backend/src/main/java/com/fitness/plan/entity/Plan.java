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

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 计划名称 */
    private String name;
    /** 适用目标（LOSE_FAT/MUSCLE_GAIN/KEEP_FIT/STRENGTH） */
    private String goal;
    /** 适用水平（BEGINNER/INTERMEDIATE/ADVANCED） */
    private String level;
    /** 计划总周数 */
    private Integer durationWeeks;
    /** 每周训练次数 */
    private Integer frequencyPerWeek;
    /** 计划描述 */
    private String description;
    /** 封面图 URL */
    private String coverImage;
    /** 状态（1=上架，0=下架） */
    private Integer status;
}
