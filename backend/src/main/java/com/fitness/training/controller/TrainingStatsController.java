package com.fitness.training.controller;

import com.fitness.common.api.Response;
import com.fitness.training.service.TrainingStatsService;
import com.fitness.training.vo.TrainingStatsSummaryVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 训练统计接口：概要统计与热力图
 */
@RestController
@RequestMapping("/api/training/stats")
@RequiredArgsConstructor
public class TrainingStatsController {

    private final TrainingStatsService trainingStatsService;

    /**
     * 训练统计概要：总次数 / 总时长 / 打卡天数 / 连续打卡 / 各动作 PR
     */
    @GetMapping("/summary")
    public Response<TrainingStatsSummaryVO> summary(@AuthenticationPrincipal Long userId) {
        return Response.ok(trainingStatsService.summary(userId));
    }
}
