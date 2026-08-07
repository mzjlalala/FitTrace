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

@Service
@RequiredArgsConstructor
public class UserPlanService {

    private final UserPlanMapper userPlanMapper;
    private final PlanMapper planMapper;
    private final PlanService planService;

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
