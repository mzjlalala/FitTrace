package com.fitness.admin.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fitness.action.entity.Action;
import lombok.Data;

import java.util.List;

/**
 * 管理后台：动作视图（含状态，供管理列表/编辑回显）
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AdminActionVO {

    private Long id;
    private Long categoryId;
    private String categoryName;
    private String name;
    private String muscleGroup;
    private String difficulty;
    private String equipment;
    private String coverImage;
    private String videoUrl;
    private String description;
    private List<String> steps;
    private List<String> tips;
    private List<String> cautions;
    private Integer status;

    public static AdminActionVO of(Action action, String categoryName) {
        AdminActionVO vo = new AdminActionVO();
        vo.setId(action.getId());
        vo.setCategoryId(action.getCategoryId());
        vo.setCategoryName(categoryName);
        vo.setName(action.getName());
        vo.setMuscleGroup(action.getMuscleGroup());
        vo.setDifficulty(action.getDifficulty());
        vo.setEquipment(action.getEquipment());
        vo.setCoverImage(action.getCoverImage());
        vo.setVideoUrl(action.getVideoUrl());
        vo.setDescription(action.getDescription());
        vo.setSteps(action.getSteps());
        vo.setTips(action.getTips());
        vo.setCautions(action.getCautions());
        vo.setStatus(action.getStatus());
        return vo;
    }
}
