package com.fitness.action.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fitness.action.entity.Action;
import lombok.Data;

/**
 * 动作列表项
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ActionListItemVO {

    /** 动作 ID */
    private Long id;
    /** 所属分类 ID */
    private Long categoryId;
    /** 所属分类名称 */
    private String categoryName;
    /** 动作名称 */
    private String name;
    /** 目标肌群（CHEST/BACK/LEGS/SHOULDERS/ARMS/CORE/CARDIO） */
    private String muscleGroup;
    /** 难度（BEGINNER/INTERMEDIATE/ADVANCED） */
    private String difficulty;
    /** 所需器械 */
    private String equipment;
    /** 封面图 URL */
    private String coverImage;
    /** 动作简介 */
    private String description;

    public static ActionListItemVO of(Action action, String categoryName) {
        ActionListItemVO vo = new ActionListItemVO();
        vo.setId(action.getId());
        vo.setCategoryId(action.getCategoryId());
        vo.setCategoryName(categoryName);
        vo.setName(action.getName());
        vo.setMuscleGroup(action.getMuscleGroup());
        vo.setDifficulty(action.getDifficulty());
        vo.setEquipment(action.getEquipment());
        vo.setCoverImage(action.getCoverImage());
        vo.setDescription(action.getDescription());
        return vo;
    }
}
