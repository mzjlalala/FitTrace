package com.fitness.diet.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 创建/更新饮食记录请求
 */
@Data
public class DietRecordCreateRequest {

    /** 记录日期（必填） */
    @NotNull(message = "记录日期不能为空")
    private LocalDate recordDate;

    /** 餐次（BREAKFAST=早餐/LUNCH=午餐/DINNER=晚餐/SNACK=加餐） */
    @NotNull(message = "餐次不能为空")
    @Pattern(regexp = "^(BREAKFAST|LUNCH|DINNER|SNACK)$", message = "餐次仅支持 BREAKFAST/LUNCH/DINNER/SNACK")
    private String mealType;

    /** 食物 ID（必填） */
    @NotNull(message = "食物 ID 不能为空")
    private Long foodId;

    /** 食用量（克，1-5000） */
    @NotNull(message = "食用量不能为空")
    @DecimalMin(value = "1.0", message = "食用量 1-5000 克")
    @DecimalMax(value = "5000.0", message = "食用量 1-5000 克")
    private BigDecimal quantityG;
}
