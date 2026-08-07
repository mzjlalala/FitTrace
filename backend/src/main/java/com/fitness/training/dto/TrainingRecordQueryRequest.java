package com.fitness.training.dto;

import lombok.Data;

import java.time.LocalDate;

/**
 * 训练记录分页查询请求（POST body）
 */
@Data
public class TrainingRecordQueryRequest {

    /** 页码（从 1 开始，默认 1） */
    private Long page;
    /** 每页条数（默认 10） */
    private Long size;
    /** 起始日期筛选（含） */
    private LocalDate startDate;
    /** 结束日期筛选（含） */
    private LocalDate endDate;
}
