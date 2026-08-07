package com.fitness.admin.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.action.entity.Action;
import com.fitness.action.mapper.ActionMapper;
import com.fitness.admin.dto.AdminPlanRequest;
import com.fitness.admin.vo.AdminPlanVO;
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
import com.fitness.training.entity.TrainingRecord;
import com.fitness.training.mapper.TrainingRecordMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 管理后台计划服务：全量列表（含下架）、计划树整体创建/替换、下架
 */
@Service
@RequiredArgsConstructor
public class AdminPlanService {

    private final PlanMapper planMapper;
    private final PlanWeekMapper planWeekMapper;
    private final PlanDayMapper planDayMapper;
    private final PlanDayActionMapper planDayActionMapper;
    private final ActionMapper actionMapper;
    private final TrainingRecordMapper trainingRecordMapper;

    /**
     * 全量分页（含下架）：名称关键字/目标筛选，按 id 倒序
     */
    public IPage<AdminPlanVO> list(String keyword, String goal, long page, long size) {
        Page<Plan> p = new Page<>(page, size);
        planMapper.selectPage(p, Wrappers.<Plan>lambdaQuery()
                .like(keyword != null && !keyword.isBlank(), Plan::getName, keyword)
                .eq(goal != null && !goal.isBlank(), Plan::getGoal, goal)
                .orderByDesc(Plan::getId));
        Page<AdminPlanVO> result = new Page<>(p.getCurrent(), p.getSize(), p.getTotal());
        result.setRecords(p.getRecords().stream().map(AdminPlanVO::of).toList());
        return result;
    }

    /**
     * 创建计划树（plan → weeks → days → actions，事务）；动作必须全部存在
     */
    @Transactional
    public AdminPlanVO create(AdminPlanRequest req) {
        validateActions(req);
        Plan plan = new Plan();
        apply(plan, req);
        plan.setStatus(1);
        planMapper.insert(plan);
        insertTree(plan.getId(), req);
        return AdminPlanVO.of(planMapper.selectById(plan.getId()));
    }

    /**
     * 整体替换计划树；该计划已被训练记录引用时拒绝（删除 plan_day 会破坏外键），抛 409
     */
    @Transactional
    public AdminPlanVO update(Long id, AdminPlanRequest req) {
        Plan plan = planMapper.selectById(id);
        if (plan == null) {
            throw new BizException(ResultCode.NOT_FOUND, "计划不存在");
        }
        Long recordCount = trainingRecordMapper.selectCount(
                Wrappers.<TrainingRecord>lambdaQuery().eq(TrainingRecord::getPlanId, id));
        if (recordCount > 0) {
            throw new BizException(ResultCode.CONFLICT, "该计划已有训练记录，无法修改编排");
        }
        validateActions(req);
        apply(plan, req);
        planMapper.updateById(plan);
        deleteTree(id);
        insertTree(id, req);
        return AdminPlanVO.of(planMapper.selectById(id));
    }

    /**
     * 下架计划（软删除 status=0，树保留）；不存在抛 404
     */
    @Transactional
    public void delete(Long id) {
        Plan plan = planMapper.selectById(id);
        if (plan == null) {
            throw new BizException(ResultCode.NOT_FOUND, "计划不存在");
        }
        plan.setStatus(0);
        planMapper.updateById(plan);
    }

    /**
     * 校验计划树中引用的动作全部存在（in 批查数量比对）
     */
    private void validateActions(AdminPlanRequest req) {
        List<Long> actionIds = req.getWeeks().stream()
                .flatMap(w -> w.getDays().stream())
                .filter(d -> d.getActions() != null)
                .flatMap(d -> d.getActions().stream())
                .map(AdminPlanRequest.AdminDayActionRequest::getActionId)
                .distinct().toList();
        if (actionIds.isEmpty()) {
            return;
        }
        Long found = actionMapper.selectCount(
                Wrappers.<Action>lambdaQuery().in(Action::getId, actionIds));
        if (found != actionIds.size()) {
            throw new BizException(ResultCode.NOT_FOUND, "动作不存在");
        }
    }

    /**
     * 插入计划树：周→日→动作（sort 按提交顺序，空值归一）
     */
    private void insertTree(Long planId, AdminPlanRequest req) {
        for (AdminPlanRequest.AdminWeekRequest w : req.getWeeks()) {
            PlanWeek week = new PlanWeek();
            week.setPlanId(planId);
            week.setWeekNo(w.getWeekNo());
            planWeekMapper.insert(week);
            for (AdminPlanRequest.AdminDayRequest d : w.getDays()) {
                PlanDay day = new PlanDay();
                day.setPlanWeekId(week.getId());
                day.setDayNo(d.getDayNo());
                day.setRestFlag(d.getRestFlag() != null && d.getRestFlag());
                day.setTitle(d.getTitle());
                planDayMapper.insert(day);
                if (d.getActions() == null) {
                    continue;
                }
                int sort = 1;
                for (AdminPlanRequest.AdminDayActionRequest a : d.getActions()) {
                    PlanDayAction pda = new PlanDayAction();
                    pda.setPlanDayId(day.getId());
                    pda.setActionId(a.getActionId());
                    pda.setSort(a.getSort() == null ? sort : a.getSort());
                    pda.setSets(a.getSets());
                    pda.setReps(a.getReps());
                    pda.setWeightMode(a.getWeightMode());
                    pda.setRestSeconds(a.getRestSeconds());
                    planDayActionMapper.insert(pda);
                    sort++;
                }
            }
        }
    }

    /**
     * 删除计划树（actions → days → weeks，由内向外）
     */
    private void deleteTree(Long planId) {
        List<PlanWeek> weeks = planWeekMapper.selectList(
                Wrappers.<PlanWeek>lambdaQuery().eq(PlanWeek::getPlanId, planId));
        List<Long> weekIds = weeks.stream().map(PlanWeek::getId).toList();
        if (weekIds.isEmpty()) {
            return;
        }
        List<PlanDay> days = planDayMapper.selectList(
                Wrappers.<PlanDay>lambdaQuery().in(PlanDay::getPlanWeekId, weekIds));
        List<Long> dayIds = days.stream().map(PlanDay::getId).toList();
        if (!dayIds.isEmpty()) {
            planDayActionMapper.delete(Wrappers.<PlanDayAction>lambdaQuery()
                    .in(PlanDayAction::getPlanDayId, dayIds));
            planDayMapper.deleteBatchIds(dayIds);
        }
        planWeekMapper.deleteBatchIds(weekIds);
    }

    private void apply(Plan plan, AdminPlanRequest req) {
        plan.setName(req.getName());
        plan.setGoal(req.getGoal());
        plan.setLevel(req.getLevel());
        plan.setDurationWeeks(req.getDurationWeeks());
        plan.setFrequencyPerWeek(req.getFrequencyPerWeek());
        plan.setDescription(req.getDescription());
    }
}
