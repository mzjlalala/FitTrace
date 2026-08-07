package com.fitness.system.mapper;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fitness.system.entity.SysUser;
import com.fitness.system.entity.UserProfile;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class SysUserMapperTest {

    @Autowired
    private SysUserMapper sysUserMapper;
    @Autowired
    private UserProfileMapper userProfileMapper;

    @Test
    void insertAndSelectUser_withAutoFill() {
        SysUser user = new SysUser();
        user.setUsername("tester1");
        user.setPassword("$2a$10$placeholder");
        user.setNickname("Tester");
        user.setStatus(1);
        sysUserMapper.insert(user);
        assertThat(user.getId()).isNotNull();
        assertThat(user.getCreatedAt()).isNotNull();

        SysUser loaded = sysUserMapper.selectById(user.getId());
        assertThat(loaded.getUsername()).isEqualTo("tester1");
        assertThat(loaded.getPassword()).isEqualTo("$2a$10$placeholder");
    }

    @Test
    void insertProfile_forUser() {
        SysUser user = new SysUser();
        user.setUsername("tester2");
        user.setPassword("x");
        user.setStatus(1);
        sysUserMapper.insert(user);

        UserProfile profile = new UserProfile();
        profile.setUserId(user.getId());
        profile.setHeightCm(new BigDecimal("175.0"));
        profile.setWeightKg(new BigDecimal("72.5"));
        userProfileMapper.insert(profile);

        UserProfile loaded = userProfileMapper.selectOne(
                Wrappers.<UserProfile>lambdaQuery().eq(UserProfile::getUserId, user.getId()));
        assertThat(loaded.getHeightCm()).isEqualByComparingTo("175.0");
        assertThat(loaded.getWeightKg()).isEqualByComparingTo("72.5");
    }

    @Test
    void duplicateUsername_violatesDbUniqueConstraint() {
        SysUser user = new SysUser();
        user.setUsername("dup-user");
        user.setPassword("x");
        user.setStatus(1);
        sysUserMapper.insert(user);

        SysUser dup = new SysUser();
        dup.setUsername("dup-user");
        dup.setPassword("x");
        dup.setStatus(1);
        assertThatThrownBy(() -> sysUserMapper.insert(dup))
                .isInstanceOf(DuplicateKeyException.class);
    }
}
