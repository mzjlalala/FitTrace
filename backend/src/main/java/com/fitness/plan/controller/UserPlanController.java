package com.fitness.plan.controller;

import com.fitness.common.api.Response;
import com.fitness.plan.dto.UserPlanStartRequest;
import com.fitness.plan.dto.UserPlanUpdateRequest;
import com.fitness.plan.service.UserPlanService;
import com.fitness.plan.vo.UserPlanVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 计划订阅接口：开始计划 / 我的订阅 / 更新订阅状态
 */
@RestController
@RequestMapping("/api/user-plans")
@RequiredArgsConstructor
public class UserPlanController {

    private final UserPlanService userPlanService;

    /**
     * 开始（订阅）一个计划，同一计划重复订阅 ACTIVE 将返回 409
     */
    @PostMapping
    public Response<UserPlanVO> start(@AuthenticationPrincipal Long userId,
                                      @Valid @RequestBody UserPlanStartRequest req) {
        return Response.ok(userPlanService.start(userId, req.getPlanId()));
    }

    /**
     * 获取当前用户全部订阅记录（含计划概要，按开始日期倒序）
     */
    @GetMapping
    public Response<List<UserPlanVO>> listMine(@AuthenticationPrincipal Long userId) {
        return Response.ok(userPlanService.listMine(userId));
    }

    /**
     * 更新订阅状态（COMPLETED=已完成 / QUIT=已退出），仅本人可操作
     */
    @PutMapping("/{id}")
    public Response<UserPlanVO> updateStatus(@AuthenticationPrincipal Long userId,
                                             @PathVariable Long id,
                                             @Valid @RequestBody UserPlanUpdateRequest req) {
        return Response.ok(userPlanService.updateStatus(userId, id, req.getStatus()));
    }
}
