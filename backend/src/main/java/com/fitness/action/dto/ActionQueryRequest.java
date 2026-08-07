package com.fitness.action.dto;

import lombok.Data;

/**
 * 动作分页查询请求（POST body）
 */
@Data
public class ActionQueryRequest {

    /** 页码（从 1 开始，默认 1） */
    private Long page;
    /** 每页条数（默认 10） */
    private Long size;
    /** 分类 ID 筛选 */
    private Long categoryId;
    /** 肌群筛选（CHEST/BACK/LEGS/SHOULDERS/BICEPS/TRICEPS/CORE/CARDIO） */
    private String muscleGroup;
    /** 难度筛选（BEGINNER/INTERMEDIATE/ADVANCED） */
    private String difficulty;
    /** 名称关键字（模糊） */
    private String keyword;
}
