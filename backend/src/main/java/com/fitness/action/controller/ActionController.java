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

@RestController
@RequestMapping("/api/actions")
@RequiredArgsConstructor
public class ActionController {

    private final ActionService actionService;

    @GetMapping("/categories")
    public Response<List<ActionCategory>> categories() {
        return Response.ok(actionService.listCategories());
    }

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

    @GetMapping("/{id}")
    public Response<ActionDetailVO> detail(@PathVariable Long id) {
        return Response.ok(actionService.getActionDetail(id));
    }
}
