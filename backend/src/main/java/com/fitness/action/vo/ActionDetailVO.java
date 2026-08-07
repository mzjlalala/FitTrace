package com.fitness.action.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fitness.action.entity.Action;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 动作详情（在列表项基础上追加教程内容）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ActionDetailVO extends ActionListItemVO {

    private String videoUrl;
    private List<String> steps;
    private List<String> tips;
    private List<String> cautions;

    public static ActionDetailVO of(Action action, String categoryName) {
        ActionDetailVO vo = new ActionDetailVO();
        vo.setId(action.getId());
        vo.setCategoryId(action.getCategoryId());
        vo.setCategoryName(categoryName);
        vo.setName(action.getName());
        vo.setMuscleGroup(action.getMuscleGroup());
        vo.setDifficulty(action.getDifficulty());
        vo.setEquipment(action.getEquipment());
        vo.setCoverImage(action.getCoverImage());
        vo.setDescription(action.getDescription());
        vo.setVideoUrl(action.getVideoUrl());
        vo.setSteps(action.getSteps());
        vo.setTips(action.getTips());
        vo.setCautions(action.getCautions());
        return vo;
    }
}
