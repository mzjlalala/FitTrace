package com.fitness.diet.dto;

import lombok.Data;

/**
 * 食物库分页查询请求（POST body）
 */
@Data
public class DietFoodQueryRequest {

    /** 页码（从 1 开始，默认 1） */
    private Long page;
    /** 每页条数（默认 10） */
    private Long size;
    /** 名称关键字（模糊） */
    private String keyword;
    /** 分类筛选（主食/肉蛋/蔬菜/水果/奶类/其他） */
    private String category;
}
