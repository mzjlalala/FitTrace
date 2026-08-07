package com.fitness.diet.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 食物（营养数据按每 100g 存储）
 */
@Data
@TableName("diet_food")
public class DietFood {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 食物名称 */
    private String name;
    /** 分类（主食/肉蛋/蔬菜/水果/奶类/其他） */
    private String category;
    /** 每 100g 热量（千卡） */
    @TableField("calories_per_100g")
    private BigDecimal caloriesPer100g;
    /** 每 100g 蛋白质（g） */
    @TableField("protein_per_100g")
    private BigDecimal proteinPer100g;
    /** 每 100g 脂肪（g） */
    @TableField("fat_per_100g")
    private BigDecimal fatPer100g;
    /** 每 100g 碳水化合物（g） */
    @TableField("carb_per_100g")
    private BigDecimal carbPer100g;
    /** 图片 URL（OSS 上传） */
    private String image;
    /** 状态（1=上架，0=下架） */
    private Integer status;
}
