package com.fitness.training.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fitness.training.entity.TrainingRecord;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 训练记录概要（含计划名）
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TrainingRecordVO {

    private Long id;
    private Long planId;
    private Long planDayId;
    private LocalDate trainingDate;
    private Integer durationMinutes;
    private String feel;
    private String note;
    private String planName;
    private LocalDateTime createdAt;

    public static TrainingRecordVO of(TrainingRecord record, String planName) {
        TrainingRecordVO vo = new TrainingRecordVO();
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
