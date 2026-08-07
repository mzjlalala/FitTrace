package com.fitness.admin.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 管理后台：食物创建/更新请求
 */
@Data
public class AdminFoodRequest {

    /** 食物名称（必填） */
    @NotBlank(message = "食物名称不能为空")
    @Size(max = 100, message = "食物名称最长 100 字")
    private String name;

    /** 分类（主食/肉蛋/蔬菜/水果/奶类/其他） */
    @Size(max = 30, message = "分类最长 30 字")
    private String category;

    /** 每 100g 热量（千卡，必填，0-1000） */
    @NotNull(message = "每 100g 热量不能为空")
    @DecimalMin(value = "0.0", message = "热量范围 0-1000")
    @DecimalMax(value = "1000.0", message = "热量范围 0-1000")
    private BigDecimal caloriesPer100g;

    /** 每 100g 蛋白质（g） */
    @DecimalMin(value = "0.0", message = "蛋白质不能为负")
    @DecimalMax(value = "100.0", message = "蛋白质范围 0-100")
    private BigDecimal proteinPer100g;

    /** 每 100g 脂肪（g） */
    @DecimalMin(value = "0.0", message = "脂肪不能为负")
    @DecimalMax(value = "100.0", message = "脂肪范围 0-100")
    private BigDecimal fatPer100g;

    /** 每 100g 碳水化合物（g） */
    @DecimalMin(value = "0.0", message = "碳水不能为负")
    @DecimalMax(value = "100.0", message = "碳水范围 0-100")
    private BigDecimal carbPer100g;

    /** 状态（1=上架，0=下架） */
    @Min(value = 0, message = "状态只能是 0 或 1")
    @Max(value = 1, message = "状态只能是 0 或 1")
    private Integer status;
}
