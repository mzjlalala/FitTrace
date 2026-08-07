package com.fitness.admin.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fitness.diet.entity.DietFood;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 管理后台：食物视图（含状态）
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AdminFoodVO {

    private Long id;
    private String name;
    private String category;
    private BigDecimal caloriesPer100g;
    private BigDecimal proteinPer100g;
    private BigDecimal fatPer100g;
    private BigDecimal carbPer100g;
    private Integer status;

    public static AdminFoodVO of(DietFood food) {
        AdminFoodVO vo = new AdminFoodVO();
        vo.setId(food.getId());
        vo.setName(food.getName());
        vo.setCategory(food.getCategory());
        vo.setCaloriesPer100g(food.getCaloriesPer100g());
        vo.setProteinPer100g(food.getProteinPer100g());
        vo.setFatPer100g(food.getFatPer100g());
        vo.setCarbPer100g(food.getCarbPer100g());
        vo.setStatus(food.getStatus());
        return vo;
    }
}
