package com.fitness.training.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fitness.action.entity.Action;
import com.fitness.action.mapper.ActionMapper;
import com.fitness.training.entity.TrainingRecord;
import com.fitness.training.mapper.TrainingRecordMapper;
import com.fitness.training.mapper.TrainingRecordSetMapper;
import com.fitness.training.vo.PrItemVO;
import com.fitness.training.vo.TrainingStatsSummaryVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 训练统计服务：概要（次数/时长/打卡/连续/PR）与热力图（365 天）
 */
@Service
@RequiredArgsConstructor
public class TrainingStatsService {

    private final TrainingRecordMapper trainingRecordMapper;
    private final TrainingRecordSetMapper trainingRecordSetMapper;
    private final ActionMapper actionMapper;

    /**
     * 统计概要：总次数/总时长（SUM 空值按 0）/打卡天数（日期去重）；
     * 连续打卡从今天往回数（今天无记录则从昨天开始）；PR 每个动作取完成组中的最佳组
     */
    public TrainingStatsSummaryVO summary(Long userId) {
        Map<String, Object> row = trainingRecordMapper.selectSummary(userId);
        TrainingStatsSummaryVO vo = new TrainingStatsSummaryVO();
        vo.setTotalCount(((Number) row.get("total")).longValue());
        vo.setTotalMinutes(((Number) row.get("minutes")).longValue());
        vo.setCheckInDays(((Number) row.get("days")).longValue());
        vo.setStreakDays(calcStreak(userId));

        List<TrainingRecordSetMapper.PrRow> prRows = trainingRecordSetMapper.selectPrRows(userId);
        List<Long> actionIds = prRows.stream().map(TrainingRecordSetMapper.PrRow::getActionId).toList();
        Map<Long, String> actionNames = actionIds.isEmpty() ? Map.of()
                : actionMapper.selectBatchIds(actionIds).stream()
                        .collect(Collectors.toMap(Action::getId, Action::getName));
        vo.setPrList(prRows.stream()
                .map(r -> PrItemVO.of(r, actionNames.get(r.getActionId())))
                .toList());
        return vo;
    }

    /**
     * 连续打卡天数：从今天往回数连续有记录的天数（取最近 366 天数据计算）
     */
    private Integer calcStreak(Long userId) {
        Set<LocalDate> dates = trainingRecordMapper.selectList(Wrappers.<TrainingRecord>lambdaQuery()
                        .select(TrainingRecord::getTrainingDate)
                        .eq(TrainingRecord::getUserId, userId)
                        .ge(TrainingRecord::getTrainingDate, LocalDate.now().minusDays(366)))
                .stream().map(TrainingRecord::getTrainingDate).collect(Collectors.toSet());
        int streak = 0;
        LocalDate cursor = LocalDate.now();
        if (!dates.contains(cursor)) {
            cursor = cursor.minusDays(1);
        }
        while (dates.contains(cursor)) {
            streak++;
            cursor = cursor.minusDays(1);
        }
        return streak;
    }
}
