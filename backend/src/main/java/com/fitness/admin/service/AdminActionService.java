package com.fitness.admin.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.action.entity.Action;
import com.fitness.action.entity.ActionCategory;
import com.fitness.action.mapper.ActionCategoryMapper;
import com.fitness.action.mapper.ActionMapper;
import com.fitness.admin.dto.AdminActionRequest;
import com.fitness.admin.vo.AdminActionVO;
import com.fitness.common.api.ResultCode;
import com.fitness.common.exception.BizException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 管理后台动作服务：全量列表（含下架）、创建、编辑、下架/上架
 */
@Service
@RequiredArgsConstructor
public class AdminActionService {

    private final ActionMapper actionMapper;
    private final ActionCategoryMapper actionCategoryMapper;

    /**
     * 全量分页（含下架数据）：名称关键字/分类筛选，按 id 倒序
     */
    public IPage<AdminActionVO> list(String keyword, Long categoryId, long page, long size) {
        Page<Action> p = new Page<>(page, size);
        actionMapper.selectPage(p, Wrappers.<Action>lambdaQuery()
                .like(keyword != null && !keyword.isBlank(), Action::getName, keyword)
                .eq(categoryId != null, Action::getCategoryId, categoryId)
                .orderByAsc(Action::getId));

        List<Long> categoryIds = p.getRecords().stream()
                .map(Action::getCategoryId).filter(Objects::nonNull).distinct().toList();
        Map<Long, String> categoryNames = categoryIds.isEmpty() ? Map.of()
                : actionCategoryMapper.selectBatchIds(categoryIds).stream()
                        .collect(Collectors.toMap(ActionCategory::getId, ActionCategory::getName));

        Page<AdminActionVO> result = new Page<>(p.getCurrent(), p.getSize(), p.getTotal());
        result.setRecords(p.getRecords().stream()
                .map(a -> AdminActionVO.of(a,
                        a.getCategoryId() == null ? null : categoryNames.get(a.getCategoryId())))
                .toList());
        return result;
    }

    /**
     * 创建动作：分类存在性校验（非空时），steps/tips/cautions 空值归一为空数组
     */
    @Transactional
    public AdminActionVO create(AdminActionRequest req) {
        validateCategory(req.getCategoryId());
        Action action = new Action();
        apply(action, req);
        action.setStatus(req.getStatus() == null ? 1 : req.getStatus());
        actionMapper.insert(action);
        return AdminActionVO.of(action, categoryName(action.getCategoryId()));
    }

    /**
     * 编辑动作（可修改状态实现上/下架）；不存在抛 404
     */
    @Transactional
    public AdminActionVO update(Long id, AdminActionRequest req) {
        Action action = actionMapper.selectById(id);
        if (action == null) {
            throw new BizException(ResultCode.NOT_FOUND, "动作不存在");
        }
        validateCategory(req.getCategoryId());
        apply(action, req);
        actionMapper.updateById(action);
        return AdminActionVO.of(action, categoryName(action.getCategoryId()));
    }

    /**
     * 下架动作（软删除 status=0）；不存在抛 404
     */
    @Transactional
    public void delete(Long id) {
        Action action = actionMapper.selectById(id);
        if (action == null) {
            throw new BizException(ResultCode.NOT_FOUND, "动作不存在");
        }
        action.setStatus(0);
        actionMapper.updateById(action);
    }

    private void validateCategory(Long categoryId) {
        if (categoryId != null && actionCategoryMapper.selectById(categoryId) == null) {
            throw new BizException(ResultCode.NOT_FOUND, "分类不存在");
        }
    }

    private String categoryName(Long categoryId) {
        if (categoryId == null) {
            return null;
        }
        ActionCategory category = actionCategoryMapper.selectById(categoryId);
        return category == null ? null : category.getName();
    }

    private void apply(Action action, AdminActionRequest req) {
        action.setName(req.getName());
        action.setCategoryId(req.getCategoryId());
        action.setMuscleGroup(req.getMuscleGroup());
        action.setDifficulty(req.getDifficulty());
        action.setEquipment(req.getEquipment());
        action.setCoverImage(req.getCoverImage());
        action.setVideoUrl(req.getVideoUrl());
        action.setDescription(req.getDescription());
        action.setSteps(req.getSteps() == null ? List.of() : req.getSteps());
        action.setTips(req.getTips() == null ? List.of() : req.getTips());
        action.setCautions(req.getCautions() == null ? List.of() : req.getCautions());
        action.setStatus(req.getStatus());
    }
}
