package com.fitness.admin.dto;

import lombok.Data;

/**
 * 管理后台：动作分页查询请求（POST body）
 */
@Data
public class AdminActionQueryRequest {

    /** 页码（从 1 开始，默认 1） */
    private Long page;
    /** 每页条数（默认 10） */
    private Long size;
    /** 名称关键字（模糊） */
    private String keyword;
    /** 分类 ID 筛选 */
    private Long categoryId;
}
