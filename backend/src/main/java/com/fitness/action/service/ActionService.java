package com.fitness.action.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.action.entity.Action;
import com.fitness.action.entity.ActionCategory;
import com.fitness.action.mapper.ActionCategoryMapper;
import com.fitness.action.mapper.ActionMapper;
import com.fitness.action.vo.ActionListItemVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ActionService {

    private final ActionMapper actionMapper;
    private final ActionCategoryMapper actionCategoryMapper;

    public List<ActionCategory> listCategories() {
        return actionCategoryMapper.selectList(
                Wrappers.<ActionCategory>lambdaQuery().orderByAsc(ActionCategory::getSort));
    }

    public IPage<ActionListItemVO> listActions(Long categoryId, String muscleGroup, String difficulty,
                                               String keyword, long page, long size) {
        Page<Action> p = new Page<>(page, size);
        actionMapper.selectPage(p, Wrappers.<Action>lambdaQuery()
                .eq(Action::getStatus, 1)
                .eq(categoryId != null, Action::getCategoryId, categoryId)
                .eq(muscleGroup != null && !muscleGroup.isBlank(), Action::getMuscleGroup, muscleGroup)
                .eq(difficulty != null && !difficulty.isBlank(), Action::getDifficulty, difficulty)
                .like(keyword != null && !keyword.isBlank(), Action::getName, keyword)
                .orderByAsc(Action::getId));

        List<Long> categoryIds = p.getRecords().stream()
                .map(Action::getCategoryId).filter(Objects::nonNull).distinct().toList();
        Map<Long, String> categoryNames = categoryIds.isEmpty() ? Map.of()
                : actionCategoryMapper.selectBatchIds(categoryIds).stream()
                        .collect(Collectors.toMap(ActionCategory::getId, ActionCategory::getName));

        Page<ActionListItemVO> result = new Page<>(p.getCurrent(), p.getSize(), p.getTotal());
        result.setRecords(p.getRecords().stream()
                .map(a -> ActionListItemVO.of(a,
                        a.getCategoryId() == null ? null : categoryNames.get(a.getCategoryId())))
                .toList());
        return result;
    }
}
