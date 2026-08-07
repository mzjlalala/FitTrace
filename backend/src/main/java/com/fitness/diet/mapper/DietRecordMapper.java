package com.fitness.diet.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fitness.diet.entity.DietRecord;
import lombok.Data;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 饮食记录 Mapper
 */
@Mapper
public interface DietRecordMapper extends BaseMapper<DietRecord> {

    /**
     * 每日营养汇总：范围内每天按食物换算求和（营养 = 每 100g 值 × 克数 ÷ 100，保留 1 位）
     */
    @Select("""
            SELECT r.record_date AS recordDate,
                   ROUND(SUM(f.calories_per_100g * r.quantity_g / 100), 1) AS caloriesKcal,
                   ROUND(SUM(f.protein_per_100g * r.quantity_g / 100), 1) AS proteinG,
                   ROUND(SUM(f.fat_per_100g * r.quantity_g / 100), 1)     AS fatG,
                   ROUND(SUM(f.carb_per_100g * r.quantity_g / 100), 1)    AS carbG
            FROM diet_record r
            JOIN diet_food f ON f.id = r.food_id
            WHERE r.user_id = #{userId} AND r.record_date BETWEEN #{startDate} AND #{endDate}
            GROUP BY r.record_date
            ORDER BY r.record_date
            """)
    List<SummaryRow> selectSummary(@Param("userId") Long userId,
                                   @Param("startDate") LocalDate startDate,
                                   @Param("endDate") LocalDate endDate);

    /**
     * 汇总查询行（某天的营养合计）
     */
    @Data
    class SummaryRow {
        private LocalDate recordDate;
        private BigDecimal caloriesKcal;
        private BigDecimal proteinG;
        private BigDecimal fatG;
        private BigDecimal carbG;
    }
}
