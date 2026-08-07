package com.fitness.plan.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fitness.action.entity.Action;
import com.fitness.action.mapper.ActionMapper;
import com.fitness.common.api.ResultCode;
import com.fitness.common.exception.BizException;
import com.fitness.plan.entity.Plan;
import com.fitness.plan.entity.PlanDay;
import com.fitness.plan.entity.PlanDayAction;
import com.fitness.plan.entity.PlanWeek;
import com.fitness.plan.mapper.PlanDayActionMapper;
import com.fitness.plan.mapper.PlanDayMapper;
import com.fitness.plan.mapper.PlanMapper;
import com.fitness.plan.mapper.PlanWeekMapper;
import com.fitness.plan.vo.ActionBriefVO;
import com.fitness.plan.vo.PlanDayActionVO;
import com.fitness.plan.vo.PlanDayVO;
import com.fitness.plan.vo.PlanDetailVO;
import com.fitness.plan.vo.PlanVO;
import com.fitness.plan.vo.PlanWeekVO;
import com.fitness.system.entity.UserProfile;
import com.fitness.system.mapper.UserProfileMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 训练计划服务：列表筛选、按用户资料推荐、详情树组装
 */
@Service
@RequiredArgsConstructor
public class PlanService {

    private final PlanMapper planMapper;
    private final PlanWeekMapper planWeekMapper;
    private final PlanDayMapper planDayMapper;
    private final PlanDayActionMapper planDayActionMapper;
    private final ActionMapper actionMapper;
    private final UserProfileMapper userProfileMapper;

    /**
     * 查询上架计划，可按目标/水平筛选，按 id 升序
     */
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

    /**
     * 按 id 校验并返回上架计划，不存在或已下架抛 404 业务异常
     */
    public Plan requirePlan(Long id) {
        Plan plan = planMapper.selectById(id);
        if (plan == null || plan.getStatus() == null || plan.getStatus() != 1) {
            throw new BizException(ResultCode.NOT_FOUND, "计划不存在");
        }
        return plan;
    }

    /**
     * 计划详情：plan → weeks → days → actions，逐层 in 批查后在内存组装。
     */
    public PlanDetailVO getPlanDetail(Long id) {
        Plan plan = requirePlan(id);
        PlanDetailVO vo = PlanDetailVO.of(plan);

        List<PlanWeek> weeks = planWeekMapper.selectList(Wrappers.<PlanWeek>lambdaQuery()
                .eq(PlanWeek::getPlanId, id).orderByAsc(PlanWeek::getWeekNo));
        List<Long> weekIds = weeks.stream().map(PlanWeek::getId).toList();
        if (weekIds.isEmpty()) {
            vo.setWeeks(List.of());
            return vo;
        }

        List<PlanDay> days = planDayMapper.selectList(Wrappers.<PlanDay>lambdaQuery()
                .in(PlanDay::getPlanWeekId, weekIds).orderByAsc(PlanDay::getDayNo));
        Map<Long, List<PlanDay>> daysByWeek = days.stream()
                .collect(Collectors.groupingBy(PlanDay::getPlanWeekId));

        List<Long> dayIds = days.stream().map(PlanDay::getId).toList();
        List<PlanDayAction> pdas = dayIds.isEmpty() ? List.of() : planDayActionMapper.selectList(
                Wrappers.<PlanDayAction>lambdaQuery()
                        .in(PlanDayAction::getPlanDayId, dayIds).orderByAsc(PlanDayAction::getSort));
        Map<Long, List<PlanDayAction>> pdasByDay = pdas.stream()
                .collect(Collectors.groupingBy(PlanDayAction::getPlanDayId));

        List<Long> actionIds = pdas.stream().map(PlanDayAction::getActionId).distinct().toList();
        Map<Long, Action> actionsById = actionIds.isEmpty() ? Map.of()
                : actionMapper.selectBatchIds(actionIds).stream()
                        .collect(Collectors.toMap(Action::getId, a -> a));

        List<PlanWeekVO> weekVOs = weeks.stream().map(w -> {
            PlanWeekVO wv = PlanWeekVO.of(w);
            List<PlanDayVO> dayVOs = daysByWeek.getOrDefault(w.getId(), List.of()).stream().map(d -> {
                PlanDayVO dv = PlanDayVO.of(d);
                List<PlanDayActionVO> actionVOs = pdasByDay.getOrDefault(d.getId(), List.of()).stream()
                        .map(pda -> PlanDayActionVO.of(pda,
                                ActionBriefVO.of(actionsById.get(pda.getActionId()))))
                        .toList();
                dv.setActions(actionVOs);
                return dv;
            }).toList();
            wv.setDays(dayVOs);
            return wv;
        }).toList();
        vo.setWeeks(weekVOs);
        return vo;
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
