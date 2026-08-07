package com.fitness.plan.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fitness.common.api.ResultCode;
import com.fitness.common.exception.BizException;
import com.fitness.plan.entity.Plan;
import com.fitness.plan.entity.UserPlan;
import com.fitness.plan.mapper.PlanMapper;
import com.fitness.plan.mapper.UserPlanMapper;
import com.fitness.plan.vo.UserPlanVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 计划订阅服务：开始（订阅）计划、我的订阅列表、更新订阅状态
 */
@Service
@RequiredArgsConstructor
public class UserPlanService {

    private final UserPlanMapper userPlanMapper;
    private final PlanMapper planMapper;
    private final PlanService planService;

    /**
     * 开始计划：校验计划存在，同一计划已有 ACTIVE 订阅时抛 409，插入 ACTIVE 记录
     */
    @Transactional
    public UserPlanVO start(Long userId, Long planId) {
        planService.requirePlan(planId);
        Long activeCount = userPlanMapper.selectCount(Wrappers.<UserPlan>lambdaQuery()
                .eq(UserPlan::getUserId, userId)
                .eq(UserPlan::getPlanId, planId)
                .eq(UserPlan::getStatus, "ACTIVE"));
        if (activeCount > 0) {
            throw new BizException(ResultCode.CONFLICT, "已在训练该计划");
        }
        UserPlan up = new UserPlan();
        up.setUserId(userId);
        up.setPlanId(planId);
        up.setStartDate(LocalDate.now());
        up.setStatus("ACTIVE");
        userPlanMapper.insert(up);
        return UserPlanVO.of(up, planMapper.selectById(planId));
    }

    /**
     * 当前用户全部订阅记录（批量补计划信息，按开始日期倒序）
     */
    public List<UserPlanVO> listMine(Long userId) {
        List<UserPlan> ups = userPlanMapper.selectList(Wrappers.<UserPlan>lambdaQuery()
                .eq(UserPlan::getUserId, userId)
                .orderByDesc(UserPlan::getStartDate)
                .orderByDesc(UserPlan::getId));
        List<Long> planIds = ups.stream().map(UserPlan::getPlanId).distinct().toList();
        Map<Long, Plan> planMap = planIds.isEmpty() ? Map.of()
                : planMapper.selectBatchIds(planIds).stream()
                        .collect(Collectors.toMap(Plan::getId, p -> p));
        return ups.stream().map(up -> UserPlanVO.of(up, planMap.get(up.getPlanId()))).toList();
    }

    /**
     * 更新订阅状态（COMPLETED/QUIT）；非本人订阅抛 404
     */
    @Transactional
    public UserPlanVO updateStatus(Long userId, Long id, String status) {
        UserPlan up = userPlanMapper.selectById(id);
        if (up == null || !up.getUserId().equals(userId)) {
            throw new BizException(ResultCode.NOT_FOUND, "订阅记录不存在");
        }
        up.setStatus(status);
        userPlanMapper.updateById(up);
        return UserPlanVO.of(up, planMapper.selectById(up.getPlanId()));
    }
}
