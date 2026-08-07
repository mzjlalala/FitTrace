package com.fitness.action.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 动作分类
 */
@Data
@TableName("action_category")
public class ActionCategory {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 分类名称 */
    private String name;
    /** 分类编码（唯一，如 CHEST/BACK/LEGS） */
    private String code;
    /** 排序值（越小越靠前） */
    private Integer sort;
    /** 父分类 ID（一级分类为空） */
    private Long parentId;
}
