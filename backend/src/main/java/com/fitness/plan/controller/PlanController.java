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

@RestController
@RequestMapping("/api/plans")
@RequiredArgsConstructor
public class PlanController {

    private final PlanService planService;

    @GetMapping
    public Response<List<PlanVO>> list(@RequestParam(required = false) String goal,
                                       @RequestParam(required = false) String level) {
        return Response.ok(planService.listPlans(goal, level));
    }

    @GetMapping("/recommend")
    public Response<List<PlanVO>> recommend(@AuthenticationPrincipal Long userId) {
        return Response.ok(planService.recommend(userId));
    }

    @GetMapping("/{id}")
    public Response<PlanDetailVO> detail(@PathVariable Long id) {
        return Response.ok(planService.getPlanDetail(id));
    }
}
