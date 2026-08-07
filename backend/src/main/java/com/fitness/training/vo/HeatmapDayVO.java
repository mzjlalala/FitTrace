package com.fitness.training.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.LocalDate;

/**
 * 热力图单日数据（某天的训练记录条数，无记录为 0）
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HeatmapDayVO {

    /** 日期 */
    private LocalDate date;
    /** 当天训练记录条数 */
    private Long count;
}
