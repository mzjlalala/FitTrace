package com.fitness.action.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fitness.action.entity.Action;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ActionListItemVO {

    private Long id;
    private Long categoryId;
    private String categoryName;
    private String name;
    private String muscleGroup;
    private String difficulty;
    private String equipment;
    private String coverImage;
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
