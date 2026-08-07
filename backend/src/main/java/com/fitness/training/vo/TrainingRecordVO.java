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

    /** 记录 ID */
    private Long id;
    /** 关联计划 ID（自由训练可为空） */
    private Long planId;
    /** 关联计划日 ID */
    private Long planDayId;
    /** 训练日期 */
    private LocalDate trainingDate;
    /** 训练时长（分钟） */
    private Integer durationMinutes;
    /** 训练感受（GOOD=状态好/NORMAL=一般/TIRED=疲劳） */
    private String feel;
    /** 备注 */
    private String note;
    /** 计划名称（未关联计划为 null） */
    private String planName;
    /** 创建时间 */
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
