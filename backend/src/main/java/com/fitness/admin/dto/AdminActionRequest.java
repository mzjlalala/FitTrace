package com.fitness.admin.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 管理后台：动作创建/更新请求
 */
@Data
public class AdminActionRequest {

    /** 动作名称（必填） */
    @NotBlank(message = "动作名称不能为空")
    @Size(max = 100, message = "动作名称最长 100 字")
    private String name;

    /** 所属分类 ID */
    private Long categoryId;

    /** 目标肌群（CHEST/BACK/LEGS/SHOULDERS/ARMS/CORE/CARDIO） */
    private String muscleGroup;

    /** 难度（BEGINNER/INTERMEDIATE/ADVANCED） */
    private String difficulty;

    /** 所需器械 */
    private String equipment;

    /** 封面图 URL */
    private String coverImage;

    /** 教学视频 URL */
    private String videoUrl;

    /** 动作简介 */
    private String description;

    /** 动作步骤 */
    private List<String> steps;

    /** 训练技巧 */
    private List<String> tips;

    /** 注意事项/安全提醒 */
    private List<String> cautions;

    /** 状态（1=上架，0=下架） */
    @Min(value = 0, message = "状态只能是 0 或 1")
    @Max(value = 1, message = "状态只能是 0 或 1")
    private Integer status;
}
