package com.fitness.plan.controller;

import com.fitness.common.api.Response;
import com.fitness.plan.service.PlanService;
import com.fitness.plan.vo.PlanDetailVO;
import com.fitness.plan.vo.PlanVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 训练计划接口：列表、按用户资料推荐与详情
 */
@RestController
@RequestMapping("/api/plans")
@RequiredArgsConstructor
public class PlanController {

    private final PlanService planService;

    /**
     * 查询上架计划列表，可按目标/水平筛选
     */
    @GetMapping
    public Response<List<PlanVO>> list(@RequestParam(required = false) String goal,
                                       @RequestParam(required = false) String level) {
        return Response.ok(planService.listPlans(goal, level));
    }

    /**
     * 按当前用户资料规则打分推荐（goal/level/frequency 匹配加分，降序返回）
     */
    @GetMapping("/recommend")
    public Response<List<PlanVO>> recommend(@AuthenticationPrincipal Long userId) {
        return Response.ok(planService.recommend(userId));
    }

    /**
     * 获取计划详情：周 → 日 → 当日动作编排（含动作概要）
     */
    @GetMapping("/{id}")
    public Response<PlanDetailVO> detail(@PathVariable Long id) {
        return Response.ok(planService.getPlanDetail(id));
    }
}
