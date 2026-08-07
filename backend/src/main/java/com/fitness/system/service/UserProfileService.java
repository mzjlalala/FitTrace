package com.fitness.system.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fitness.common.api.ResultCode;
import com.fitness.common.exception.BizException;
import com.fitness.system.dto.UserProfileUpdateRequest;
import com.fitness.system.entity.SysUser;
import com.fitness.system.entity.UserProfile;
import com.fitness.system.mapper.SysUserMapper;
import com.fitness.system.mapper.UserProfileMapper;
import com.fitness.system.vo.UserInfoVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final SysUserMapper sysUserMapper;
    private final UserProfileMapper userProfileMapper;

    public UserInfoVO getProfile(Long userId) {
        SysUser user = requireUser(userId);
        return UserInfoVO.of(user, getOrCreateProfile(userId));
    }

    @Transactional
    public UserInfoVO updateProfile(Long userId, UserProfileUpdateRequest req) {
        SysUser user = requireUser(userId);
        if (req.getNickname() != null && !req.getNickname().isBlank()) {
            user.setNickname(req.getNickname());
            sysUserMapper.updateById(user);
        }
        UserProfile profile = getOrCreateProfile(userId);
        profile.setGender(req.getGender());
        profile.setBirthDate(req.getBirthDate());
        profile.setHeightCm(req.getHeightCm());
        profile.setWeightKg(req.getWeightKg());
        profile.setGoal(req.getGoal());
        profile.setFitnessLevel(req.getFitnessLevel());
        profile.setWeeklyFrequency(req.getWeeklyFrequency());
        userProfileMapper.updateById(profile);
        return UserInfoVO.of(user, profile);
    }

    private SysUser requireUser(Long userId) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BizException(ResultCode.NOT_FOUND, "用户不存在");
        }
        return user;
    }

    private UserProfile getOrCreateProfile(Long userId) {
        UserProfile profile = userProfileMapper.selectOne(
                Wrappers.<UserProfile>lambdaQuery().eq(UserProfile::getUserId, userId));
        if (profile == null) {
            profile = new UserProfile();
            profile.setUserId(userId);
            userProfileMapper.insert(profile);
        }
        return profile;
    }
}
