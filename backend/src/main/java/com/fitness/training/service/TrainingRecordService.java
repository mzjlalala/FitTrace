package com.fitness.training.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.action.entity.Action;
import com.fitness.action.mapper.ActionMapper;
import com.fitness.common.api.ResultCode;
import com.fitness.common.exception.BizException;
import com.fitness.plan.entity.Plan;
import com.fitness.plan.entity.PlanDay;
import com.fitness.plan.mapper.PlanDayMapper;
import com.fitness.plan.service.PlanService;
import com.fitness.training.dto.TrainingRecordCreateRequest;
import com.fitness.training.dto.TrainingSetRequest;
import com.fitness.training.entity.TrainingRecord;
import com.fitness.training.entity.TrainingRecordSet;
import com.fitness.training.mapper.TrainingRecordMapper;
import com.fitness.training.mapper.TrainingRecordSetMapper;
import com.fitness.training.vo.TrainingRecordDetailVO;
import com.fitness.training.vo.TrainingRecordVO;
import com.fitness.training.vo.TrainingSetVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 训练记录服务：创建 / 历史列表 / 详情 / 更新 / 删除（记录 + 组数据一体维护）
 */
@Service
@RequiredArgsConstructor
public class TrainingRecordService {

    private final TrainingRecordMapper trainingRecordMapper;
    private final TrainingRecordSetMapper trainingRecordSetMapper;
    private final PlanService planService;
    private final PlanDayMapper planDayMapper;
    private final ActionMapper actionMapper;

    /**
     * 创建训练记录：校验计划/计划日/动作存在性，插入记录与组数据（组序号从 1 递增），返回详情
     */
    @Transactional
    public TrainingRecordDetailVO create(Long userId, TrainingRecordCreateRequest req) {
        validateRefs(req);
        TrainingRecord record = new TrainingRecord();
        record.setUserId(userId);
        record.setPlanId(req.getPlanId());
        record.setPlanDayId(req.getPlanDayId());
        record.setTrainingDate(req.getTrainingDate());
        record.setDurationMinutes(req.getDurationMinutes());
        record.setFeel(req.getFeel());
        record.setNote(req.getNote());
        trainingRecordMapper.insert(record);
        insertSets(record.getId(), req.getSets());
        return buildDetail(record.getId());
    }

    /**
     * 校验关联引用存在性：planId 非空 → 计划必须存在；planDayId 非空 → 计划日必须存在；
     * sets 中 actionId 必须全部存在（in 批查数量比对）
     */
    private void validateRefs(TrainingRecordCreateRequest req) {
        if (req.getPlanId() != null) {
            planService.requirePlan(req.getPlanId());
        }
        if (req.getPlanDayId() != null && planDayMapper.selectById(req.getPlanDayId()) == null) {
            throw new BizException(ResultCode.NOT_FOUND, "计划日不存在");
        }
        List<Long> actionIds = req.getSets().stream()
                .map(TrainingSetRequest::getActionId).distinct().toList();
        Long found = actionMapper.selectCount(
                Wrappers.<Action>lambdaQuery().in(Action::getId, actionIds));
        if (found != actionIds.size()) {
            throw new BizException(ResultCode.NOT_FOUND, "动作不存在");
        }
    }

    /**
     * 插入组数据：组序号按提交顺序从 1 递增（不信任前端传值）
     */
    private void insertSets(Long recordId, List<TrainingSetRequest> sets) {
        for (int i = 0; i < sets.size(); i++) {
            TrainingSetRequest s = sets.get(i);
            TrainingRecordSet set = new TrainingRecordSet();
            set.setRecordId(recordId);
            set.setActionId(s.getActionId());
            set.setSetNo(i + 1);
            set.setWeightKg(s.getWeightKg());
            set.setReps(s.getReps());
            set.setDoneFlag(s.getDoneFlag() == null || s.getDoneFlag());
            trainingRecordSetMapper.insert(set);
        }
    }

    /**
     * 组装详情：记录 + 计划名 + 组数据（动作名 in 批查补齐）
     */
    private TrainingRecordDetailVO buildDetail(Long recordId) {
        TrainingRecord record = trainingRecordMapper.selectById(recordId);
        String planName = record.getPlanId() == null ? null
                : planService.requirePlan(record.getPlanId()).getName();
        TrainingRecordDetailVO vo = TrainingRecordDetailVO.of(record, planName);

        List<TrainingRecordSet> sets = trainingRecordSetMapper.selectList(
                Wrappers.<TrainingRecordSet>lambdaQuery()
                        .eq(TrainingRecordSet::getRecordId, recordId)
                        .orderByAsc(TrainingRecordSet::getSetNo));
        List<Long> actionIds = sets.stream().map(TrainingRecordSet::getActionId).distinct().toList();
        Map<Long, String> actionNames = actionIds.isEmpty() ? Map.of()
                : actionMapper.selectBatchIds(actionIds).stream()
                        .collect(Collectors.toMap(Action::getId, Action::getName));
        vo.setSets(sets.stream()
                .map(s -> TrainingSetVO.of(s, actionNames.get(s.getActionId())))
                .toList());
        return vo;
    }

    /**
     * 查询当前用户的训练记录：按日期倒序分页，可选日期范围筛选，批量补计划名
     */
    public IPage<TrainingRecordVO> listMine(Long userId, LocalDate startDate, LocalDate endDate,
                                            long page, long size) {
        Page<TrainingRecord> p = new Page<>(page, size);
        trainingRecordMapper.selectPage(p, Wrappers.<TrainingRecord>lambdaQuery()
                .eq(TrainingRecord::getUserId, userId)
                .ge(startDate != null, TrainingRecord::getTrainingDate, startDate)
                .le(endDate != null, TrainingRecord::getTrainingDate, endDate)
                .orderByDesc(TrainingRecord::getTrainingDate)
                .orderByDesc(TrainingRecord::getId));

        List<Long> planIds = p.getRecords().stream()
                .map(TrainingRecord::getPlanId).filter(Objects::nonNull).distinct().toList();
        Map<Long, String> planNames = planIds.isEmpty() ? Map.of()
                : planService.listPlans(null, null).stream()
                        .filter(pl -> planIds.contains(pl.getId()))
                        .collect(Collectors.toMap(pl -> pl.getId(), pl -> pl.getName()));

        Page<TrainingRecordVO> result = new Page<>(p.getCurrent(), p.getSize(), p.getTotal());
        result.setRecords(p.getRecords().stream()
                .map(r -> TrainingRecordVO.of(r, r.getPlanId() == null ? null
                        : planNames.get(r.getPlanId())))
                .toList());
        return result;
    }

    /**
     * 获取记录详情；不存在或非本人记录抛 404 业务异常（不泄露他人数据存在性）
     */
    public TrainingRecordDetailVO getDetail(Long userId, Long id) {
        TrainingRecord record = requireOwned(userId, id);
        String planName = record.getPlanId() == null ? null
                : planService.requirePlan(record.getPlanId()).getName();
        TrainingRecordDetailVO vo = TrainingRecordDetailVO.of(record, planName);

        List<TrainingRecordSet> sets = trainingRecordSetMapper.selectList(
                Wrappers.<TrainingRecordSet>lambdaQuery()
                        .eq(TrainingRecordSet::getRecordId, id)
                        .orderByAsc(TrainingRecordSet::getSetNo));
        List<Long> actionIds = sets.stream().map(TrainingRecordSet::getActionId).distinct().toList();
        Map<Long, String> actionNames = actionIds.isEmpty() ? Map.of()
                : actionMapper.selectBatchIds(actionIds).stream()
                        .collect(Collectors.toMap(Action::getId, Action::getName));
        vo.setSets(sets.stream()
                .map(s -> TrainingSetVO.of(s, actionNames.get(s.getActionId())))
                .toList());
        return vo;
    }

    /**
     * 按 id 校验记录归属：不存在或非本人抛 404
     */
    private TrainingRecord requireOwned(Long userId, Long id) {
        TrainingRecord record = trainingRecordMapper.selectById(id);
        if (record == null || !record.getUserId().equals(userId)) {
            throw new BizException(ResultCode.NOT_FOUND, "训练记录不存在");
        }
        return record;
    }
}
