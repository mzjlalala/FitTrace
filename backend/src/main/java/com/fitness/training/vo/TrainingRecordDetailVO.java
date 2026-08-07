package com.fitness.training.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fitness.training.entity.TrainingRecord;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 训练记录详情（概要 + 组数据）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TrainingRecordDetailVO extends TrainingRecordVO {

    /** 组数据（按组序号升序） */
    private List<TrainingSetVO> sets;

    public static TrainingRecordDetailVO of(TrainingRecord record, String planName) {
        TrainingRecordDetailVO vo = new TrainingRecordDetailVO();
        vo.setId(record.getId());
        vo.setPlanId(record.getPlanId());
        vo.setPlanDayId(record.getPlanDayId());
        vo.setTrainingDate(record.getTrainingDate());
        vo.setDurationMinutes(record.getDurationMinutes());
        vo.setFeel(record.getFeel());
        vo.setNote(record.getNote());
        vo.setPlanName(planName);
        vo.setCreatedAt(record.getCreatedAt());
        return vo;
    }
}
