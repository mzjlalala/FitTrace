package com.fitness.plan.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fitness.common.api.ResultCode;
import com.fitness.common.exception.BizException;
import com.fitness.plan.entity.Plan;
import com.fitness.plan.mapper.PlanMapper;
import com.fitness.plan.vo.PlanVO;
import com.fitness.system.entity.UserProfile;
import com.fitness.system.mapper.UserProfileMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PlanService {

    private final PlanMapper planMapper;
    private final UserProfileMapper userProfileMapper;

    public List<PlanVO> listPlans(String goal, String level) {
        return planMapper.selectList(Wrappers.<Plan>lambdaQuery()
                        .eq(Plan::getStatus, 1)
                        .eq(goal != null && !goal.isBlank(), Plan::getGoal, goal)
                        .eq(level != null && !level.isBlank(), Plan::getLevel, level)
                        .orderByAsc(Plan::getId))
                .stream().map(PlanVO::of).toList();
    }

    /**
     * 按用户资料规则打分推荐：goal 匹配 +2、level 匹配 +2、frequency 匹配 +1；
     * 未填资料时全部 0 分，按 id 升序。
     */
    public List<PlanVO> recommend(Long userId) {
        UserProfile profile = userProfileMapper.selectOne(
                Wrappers.<UserProfile>lambdaQuery().eq(UserProfile::getUserId, userId));
        return planMapper.selectList(Wrappers.<Plan>lambdaQuery()
                        .eq(Plan::getStatus, 1))
                .stream()
                .sorted(Comparator.comparingInt((Plan p) -> score(profile, p)).reversed()
                        .thenComparing(Plan::getId))
                .map(PlanVO::of).toList();
    }

    public Plan requirePlan(Long id) {
        Plan plan = planMapper.selectById(id);
        if (plan == null || plan.getStatus() == null || plan.getStatus() != 1) {
            throw new BizException(ResultCode.NOT_FOUND, "计划不存在");
        }
        return plan;
    }

    private int score(UserProfile profile, Plan plan) {
        if (profile == null) {
            return 0;
        }
        int score = 0;
        if (profile.getGoal() != null && profile.getGoal().equals(plan.getGoal())) {
            score += 2;
        }
        if (profile.getFitnessLevel() != null && profile.getFitnessLevel().equals(plan.getLevel())) {
            score += 2;
        }
        if (profile.getWeeklyFrequency() != null
                && profile.getWeeklyFrequency().equals(plan.getFrequencyPerWeek())) {
            score += 1;
        }
        return score;
    }
}
