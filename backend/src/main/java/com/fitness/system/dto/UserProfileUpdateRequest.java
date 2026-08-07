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

@Data
public class UserProfileUpdateRequest {

    @Size(max = 30, message = "昵称最长 30 字")
    private String nickname;

    @Pattern(regexp = "^(MALE|FEMALE)$", message = "性别仅支持 MALE / FEMALE")
    private String gender;

    private LocalDate birthDate;

    @DecimalMin(value = "50.0", message = "身高范围 50-250cm")
    @DecimalMax(value = "250.0", message = "身高范围 50-250cm")
    private BigDecimal heightCm;

    @DecimalMin(value = "20.0", message = "体重范围 20-300kg")
    @DecimalMax(value = "300.0", message = "体重范围 20-300kg")
    private BigDecimal weightKg;

    @Size(max = 30, message = "目标最长 30 字")
    private String goal;

    @Size(max = 20, message = "水平最长 20 字")
    private String fitnessLevel;

    @Min(value = 0, message = "周频次 0-7")
    @Max(value = 7, message = "周频次 0-7")
    private Integer weeklyFrequency;
}
