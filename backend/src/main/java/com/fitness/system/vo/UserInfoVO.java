package com.fitness.system.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fitness.system.entity.SysUser;
import com.fitness.system.entity.UserProfile;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserInfoVO {

    private Long id;
    private String username;
    private String nickname;
    private String avatar;
    private String phone;
    private String gender;
    private LocalDate birthDate;
    private BigDecimal heightCm;
    private BigDecimal weightKg;
    private String goal;
    private String fitnessLevel;
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
