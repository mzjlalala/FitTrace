package com.fitness.training.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;

/**
 * 训练统计概要：总次数 / 总时长 / 打卡天数 / 连续打卡天数 / 各动作 PR
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TrainingStatsSummaryVO {

    /** 总训练次数 */
    private Long totalCount;
    /** 总训练时长（分钟，空值按 0） */
    private Long totalMinutes;
    /** 打卡天数（按训练日期去重） */
    private Long checkInDays;
    /** 连续打卡天数（从今天往前数，今天无记录则从昨天开始） */
    private Integer streakDays;
    /** 个人纪录列表（每个动作的最佳组） */
    private List<PrItemVO> prList;
}
