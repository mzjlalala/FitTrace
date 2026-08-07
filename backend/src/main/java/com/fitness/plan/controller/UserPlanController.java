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

@RestController
@RequestMapping("/api/user-plans")
@RequiredArgsConstructor
public class UserPlanController {

    private final UserPlanService userPlanService;

    @PostMapping
    public Response<UserPlanVO> start(@AuthenticationPrincipal Long userId,
                                      @Valid @RequestBody UserPlanStartRequest req) {
        return Response.ok(userPlanService.start(userId, req.getPlanId()));
    }

    @GetMapping
    public Response<List<UserPlanVO>> listMine(@AuthenticationPrincipal Long userId) {
        return Response.ok(userPlanService.listMine(userId));
    }

    @PutMapping("/{id}")
    public Response<UserPlanVO> updateStatus(@AuthenticationPrincipal Long userId,
                                             @PathVariable Long id,
                                             @Valid @RequestBody UserPlanUpdateRequest req) {
        return Response.ok(userPlanService.updateStatus(userId, id, req.getStatus()));
    }
}
