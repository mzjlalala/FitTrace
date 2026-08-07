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

    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String code;
    private Integer sort;
    /** 父分类 ID（一级分类为空） */
    private Long parentId;
}
