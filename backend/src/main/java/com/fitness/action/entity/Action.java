package com.fitness.action.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fitness.common.handler.JsonbTypeHandler;
import lombok.Data;
import org.apache.ibatis.type.JdbcType;

import java.util.List;

/**
 * 动作（steps/tips/cautions 为 JSONB 字符串数组）
 */
@Data
@TableName(value = "action", autoResultMap = true)
public class Action {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long categoryId;
    private String name;
    /** 主要肌群（取值同分类 code） */
    private String muscleGroup;
    /** 难度（BEGINNER/INTERMEDIATE/ADVANCED） */
    private String difficulty;
    private String equipment;
    private String coverImage;
    private String videoUrl;
    private String description;
    @TableField(typeHandler = JsonbTypeHandler.class, jdbcType = JdbcType.OTHER)
    private List<String> steps;
    @TableField(typeHandler = JsonbTypeHandler.class, jdbcType = JdbcType.OTHER)
    private List<String> tips;
    @TableField(typeHandler = JsonbTypeHandler.class, jdbcType = JdbcType.OTHER)
    private List<String> cautions;
    /** 状态（1=上架，0=下架） */
    private Integer status;
}
