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

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 所属分类 ID */
    private Long categoryId;
    /** 动作名称 */
    private String name;
    /** 主要肌群（取值同分类 code，如 CHEST/BACK） */
    private String muscleGroup;
    /** 难度（BEGINNER/INTERMEDIATE/ADVANCED） */
    private String difficulty;
    /** 所需器械 */
    private String equipment;
    /** 封面图 URL */
    private String coverImage;
    /** 教学视频 URL */
    private String videoUrl;
    /** 动作描述 */
    private String description;
    /** 步骤说明（JSONB 字符串数组，每条一步） */
    @TableField(typeHandler = JsonbTypeHandler.class, jdbcType = JdbcType.OTHER)
    private List<String> steps;
    /** 技巧提示（JSONB 字符串数组） */
    @TableField(typeHandler = JsonbTypeHandler.class, jdbcType = JdbcType.OTHER)
    private List<String> tips;
    /** 注意事项（JSONB 字符串数组） */
    @TableField(typeHandler = JsonbTypeHandler.class, jdbcType = JdbcType.OTHER)
    private List<String> cautions;
    /** 状态（1=上架，0=下架） */
    private Integer status;
}
