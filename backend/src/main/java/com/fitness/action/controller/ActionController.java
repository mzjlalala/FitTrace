package com.fitness.action.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fitness.action.dto.ActionQueryRequest;
import com.fitness.action.entity.ActionCategory;
import com.fitness.action.service.ActionService;
import com.fitness.action.vo.ActionDetailVO;
import com.fitness.action.vo.ActionListItemVO;
import com.fitness.common.api.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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
     * 分页查询动作列表（POST body 传参），支持按分类/肌群/难度/名称关键字筛选（仅返回上架动作）
     */
    @PostMapping("/query")
    public Response<IPage<ActionListItemVO>> list(@RequestBody ActionQueryRequest req) {
        long page = req.getPage() == null ? 1 : req.getPage();
        long size = req.getSize() == null ? 10 : req.getSize();
        return Response.ok(actionService.listActions(
                req.getCategoryId(), req.getMuscleGroup(), req.getDifficulty(), req.getKeyword(), page, size));
    }

    /**
     * 获取动作详情（含步骤/技巧/注意事项教程内容）
     */
    @GetMapping("/{id}")
    public Response<ActionDetailVO> detail(@PathVariable Long id) {
        return Response.ok(actionService.getActionDetail(id));
    }
}
