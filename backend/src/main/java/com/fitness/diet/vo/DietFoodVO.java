package com.fitness.diet.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fitness.diet.entity.DietFood;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 食物信息（每 100g 营养数据）
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DietFoodVO {

    /** 食物 ID */
    private Long id;
    /** 食物名称 */
    private String name;
    /** 分类（主食/肉蛋/蔬菜/水果/奶类/其他） */
    private String category;
    /** 每 100g 热量（千卡） */
    private BigDecimal caloriesPer100g;
    /** 每 100g 蛋白质（g） */
    private BigDecimal proteinPer100g;
    /** 每 100g 脂肪（g） */
    private BigDecimal fatPer100g;
    /** 每 100g 碳水化合物（g） */
    private BigDecimal carbPer100g;
    /** 图片 URL（OSS 上传） */
    private String image;

    public static DietFoodVO of(DietFood food) {
        DietFoodVO vo = new DietFoodVO();
        vo.setId(food.getId());
        vo.setName(food.getName());
        vo.setCategory(food.getCategory());
        vo.setCaloriesPer100g(food.getCaloriesPer100g());
        vo.setProteinPer100g(food.getProteinPer100g());
        vo.setFatPer100g(food.getFatPer100g());
        vo.setCarbPer100g(food.getCarbPer100g());
        vo.setImage(food.getImage());
        return vo;
    }
}
