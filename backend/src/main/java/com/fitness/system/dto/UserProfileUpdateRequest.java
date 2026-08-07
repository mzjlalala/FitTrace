package com.fitness.system.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 用户资料更新请求（身体数据整体提交，null 字段直接置空）
 */
@Data
public class UserProfileUpdateRequest {

    /** 昵称（非空才更新） */
    @Size(max = 30, message = "昵称最长 30 字")
    private String nickname;

    /** 头像 URL（OSS 上传后返回的地址，非空才更新） */
    @Size(max = 255, message = "头像地址最长 255 字")
    private String avatar;

    /** 性别（MALE=男 / FEMALE=女） */
    @Pattern(regexp = "^(MALE|FEMALE)$", message = "性别仅支持 MALE / FEMALE")
    private String gender;

    /** 出生日期 */
    private LocalDate birthDate;

    /** 身高（cm，范围 50-250） */
    @DecimalMin(value = "50.0", message = "身高范围 50-250cm")
    @DecimalMax(value = "250.0", message = "身高范围 50-250cm")
    private BigDecimal heightCm;

    /** 体重（kg，范围 20-300） */
    @DecimalMin(value = "20.0", message = "体重范围 20-300kg")
    @DecimalMax(value = "300.0", message = "体重范围 20-300kg")
    private BigDecimal weightKg;

    /** 训练目标（LOSE_FAT/MUSCLE_GAIN/KEEP_FIT/STRENGTH） */
    @Size(max = 30, message = "目标最长 30 字")
    private String goal;

    /** 健身水平（BEGINNER/INTERMEDIATE/ADVANCED） */
    @Size(max = 20, message = "水平最长 20 字")
    private String fitnessLevel;

    /** 周训练频次（0-7） */
    @Min(value = 0, message = "周频次 0-7")
    @Max(value = 7, message = "周频次 0-7")
    private Integer weeklyFrequency;
}
