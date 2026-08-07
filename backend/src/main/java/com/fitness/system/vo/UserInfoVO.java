package com.fitness.system.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fitness.system.entity.SysUser;
import com.fitness.system.entity.UserProfile;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 用户信息视图（账号信息 + 身体数据）
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserInfoVO {

    /** 用户 ID */
    private Long id;
    /** 用户名 */
    private String username;
    /** 昵称 */
    private String nickname;
    /** 头像 URL */
    private String avatar;
    /** 手机号 */
    private String phone;
    /** 性别（MALE=男 / FEMALE=女） */
    private String gender;
    /** 出生日期 */
    private LocalDate birthDate;
    /** 身高（cm） */
    private BigDecimal heightCm;
    /** 体重（kg） */
    private BigDecimal weightKg;
    /** 训练目标（LOSE_FAT/MUSCLE_GAIN/KEEP_FIT/STRENGTH） */
    private String goal;
    /** 健身水平（BEGINNER/INTERMEDIATE/ADVANCED） */
    private String fitnessLevel;
    /** 周训练频次 */
    private Integer weeklyFrequency;

    public static UserInfoVO of(SysUser user, UserProfile profile) {
        UserInfoVO vo = new UserInfoVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setAvatar(user.getAvatar());
        vo.setPhone(user.getPhone());
        if (profile != null) {
            vo.setGender(profile.getGender());
            vo.setBirthDate(profile.getBirthDate());
            vo.setHeightCm(profile.getHeightCm());
            vo.setWeightKg(profile.getWeightKg());
            vo.setGoal(profile.getGoal());
            vo.setFitnessLevel(profile.getFitnessLevel());
            vo.setWeeklyFrequency(profile.getWeeklyFrequency());
        }
        return vo;
    }
}
