package com.fitness.admin.dto;

import lombok.Data;

/**
 * 管理后台：计划分页查询请求（POST body）
 */
@Data
public class AdminPlanQueryRequest {

    /** 页码（从 1 开始，默认 1） */
    private Long page;
    /** 每页条数（默认 10） */
    private Long size;
    /** 名称关键字（模糊） */
    private String keyword;
    /** 目标筛选（LOSE_FAT/MUSCLE_GAIN/KEEP_FIT/STRENGTH） */
    private String goal;
}
