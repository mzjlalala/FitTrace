package com.fitness.diet.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 饮食记录（一笔饮食：某餐吃了多少克某种食物）
 */
@Data
@TableName("diet_record")
public class DietRecord {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 用户 ID */
    private Long userId;
    /** 记录日期 */
    private LocalDate recordDate;
    /** 餐次（BREAKFAST=早餐/LUNCH=午餐/DINNER=晚餐/SNACK=加餐） */
    private String mealType;
    /** 食物 ID */
    private Long foodId;
    /** 食用量（克） */
    private BigDecimal quantityG;
    /** 创建时间（自动填充） */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    /** 更新时间（自动填充） */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
