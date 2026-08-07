package com.fitness.diet.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 每日营养汇总（热量/蛋白质/脂肪/碳水）
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DietSummaryVO {

    /** 日期 */
    private LocalDate date;
    /** 当日摄入热量（千卡） */
    private BigDecimal caloriesKcal;
    /** 当日摄入蛋白质（g） */
    private BigDecimal proteinG;
    /** 当日摄入脂肪（g） */
    private BigDecimal fatG;
    /** 当日摄入碳水化合物（g） */
    private BigDecimal carbG;
}
