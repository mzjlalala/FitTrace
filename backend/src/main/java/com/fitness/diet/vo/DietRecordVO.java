package com.fitness.diet.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fitness.diet.entity.DietFood;
import com.fitness.diet.entity.DietRecord;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 饮食记录（含食物名与按食用量换算的营养值）
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DietRecordVO {

    /** 记录 ID */
    private Long id;
    /** 记录日期 */
    private LocalDate recordDate;
    /** 餐次（BREAKFAST/LUNCH/DINNER/SNACK） */
    private String mealType;
    /** 食物 ID */
    private Long foodId;
    /** 食物名称 */
    private String foodName;
    /** 食物分类 */
    private String category;
    /** 食用量（克） */
    private BigDecimal quantityG;
    /** 摄入热量（千卡） */
    private BigDecimal caloriesKcal;
    /** 摄入蛋白质（g） */
    private BigDecimal proteinG;
    /** 摄入脂肪（g） */
    private BigDecimal fatG;
    /** 摄入碳水化合物（g） */
    private BigDecimal carbG;

    /**
     * 营养换算：食物每 100g 营养 × 食用克数 ÷ 100，保留 1 位小数（HALF_UP）
     */
    public static DietRecordVO of(DietRecord record, DietFood food) {
        DietRecordVO vo = new DietRecordVO();
        vo.setId(record.getId());
        vo.setRecordDate(record.getRecordDate());
        vo.setMealType(record.getMealType());
        vo.setFoodId(record.getFoodId());
        vo.setQuantityG(record.getQuantityG());
        if (food != null) {
            vo.setFoodName(food.getName());
            vo.setCategory(food.getCategory());
            BigDecimal q = record.getQuantityG();
            vo.setCaloriesKcal(convert(food.getCaloriesPer100g(), q));
            vo.setProteinG(convert(food.getProteinPer100g(), q));
            vo.setFatG(convert(food.getFatPer100g(), q));
            vo.setCarbG(convert(food.getCarbPer100g(), q));
        }
        return vo;
    }

    private static BigDecimal convert(BigDecimal per100g, BigDecimal quantity) {
        return per100g.multiply(quantity)
                .divide(BigDecimal.valueOf(100), 1, java.math.RoundingMode.HALF_UP);
    }
}
