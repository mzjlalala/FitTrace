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

/**
 * 用户信息服务：个人信息与身体数据的查询与维护
 */
@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final SysUserMapper sysUserMapper;
    private final UserProfileMapper userProfileMapper;

    /**
     * 获取用户信息，身体资料缺失时自动初始化一条空记录
     */
    public UserInfoVO getProfile(Long userId) {
        SysUser user = requireUser(userId);
        return UserInfoVO.of(user, getOrCreateProfile(userId));
    }

    /**
     * 更新用户信息：昵称非空才更新；身体数据整体提交（null 字段直接置空）
     */
    @Transactional
    public UserInfoVO updateProfile(Long userId, UserProfileUpdateRequest req) {
        SysUser user = requireUser(userId);
        if (req.getNickname() != null && !req.getNickname().isBlank()) {
            user.setNickname(req.getNickname());
            sysUserMapper.updateById(user);
        }
        if (req.getAvatar() != null && !req.getAvatar().isBlank()) {
            user.setAvatar(req.getAvatar());
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
