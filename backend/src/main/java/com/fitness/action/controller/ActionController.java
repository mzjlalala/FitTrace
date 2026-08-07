package com.fitness.action.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fitness.action.entity.ActionCategory;
import com.fitness.action.service.ActionService;
import com.fitness.action.vo.ActionDetailVO;
import com.fitness.action.vo.ActionListItemVO;
import com.fitness.common.api.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 动作库接口：分类、列表筛选与详情
 */
@RestController
@RequestMapping("/api/actions")
@RequiredArgsConstructor
public class ActionController {

    private final ActionService actionService;

    /**
     * 获取动作分类列表（按 sort 升序）
     */
    @GetMapping("/categories")
    public Response<List<ActionCategory>> categories() {
        return Response.ok(actionService.listCategories());
    }

    /**
     * 分页查询动作列表，支持按分类/肌群/难度/名称关键字筛选（仅返回上架动作）
     */
    @GetMapping
    public Response<IPage<ActionListItemVO>> list(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "12") long size,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String muscleGroup,
            @RequestParam(required = false) String difficulty,
            @RequestParam(required = false) String keyword) {
        return Response.ok(actionService.listActions(categoryId, muscleGroup, difficulty, keyword, page, size));
    }

    /**
     * 获取动作详情（含步骤/技巧/注意事项教程内容）
     */
    @GetMapping("/{id}")
    public Response<ActionDetailVO> detail(@PathVariable Long id) {
        return Response.ok(actionService.getActionDetail(id));
    }
}
