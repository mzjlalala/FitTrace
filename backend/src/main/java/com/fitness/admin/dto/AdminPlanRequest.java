package com.fitness.admin.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 管理后台：计划创建/更新请求（含周/日/动作树，整体提交）
 */
@Data
public class AdminPlanRequest {

    /** 计划名称（必填） */
    @NotBlank(message = "计划名称不能为空")
    @Size(max = 100, message = "计划名称最长 100 字")
    private String name;

    /** 训练目标（LOSE_FAT/MUSCLE_GAIN/KEEP_FIT/STRENGTH） */
    private String goal;

    /** 难度水平（BEGINNER/INTERMEDIATE/ADVANCED） */
    private String level;

    /** 计划总周数 */
    private Integer durationWeeks;

    /** 每周训练频次 */
    private Integer frequencyPerWeek;

    /** 计划描述 */
    private String description;

    /** 周安排（至少 1 周） */
    @NotEmpty(message = "至少需要一个训练周")
    @Valid
    private List<AdminWeekRequest> weeks;

    /** 周请求（MVP 为单周循环，weekNo=1） */
    @Data
    public static class AdminWeekRequest {

        /** 周序号（从 1 开始） */
        @NotNull(message = "周序号不能为空")
        private Integer weekNo;

        /** 该周每日安排（至少 1 天） */
        @NotEmpty(message = "每周至少一天安排")
        @Valid
        private List<AdminDayRequest> days;
    }

    /** 日请求 */
    @Data
    public static class AdminDayRequest {

        /** 日序号（从 1 开始） */
        @NotNull(message = "日序号不能为空")
        private Integer dayNo;

        /** 是否休息日（TRUE=休息日无动作） */
        private Boolean restFlag;

        /** 当日主题（如"推日"/"全身A"） */
        private String title;

        /** 当日动作编排（休息日可为空） */
        @Valid
        private List<AdminDayActionRequest> actions;
    }

    /** 当日动作请求 */
    @Data
    public static class AdminDayActionRequest {

        /** 动作 ID（必填） */
        @NotNull(message = "动作 ID 不能为空")
        private Long actionId;

        /** 当日执行顺序 */
        private Integer sort;

        /** 目标组数 */
        private Integer sets;

        /** 目标每组次数 */
        private Integer reps;

        /** 重量模式（FIXED=固定重量/递增） */
        private String weightMode;

        /** 组间休息（秒） */
        private Integer restSeconds;
    }
}
