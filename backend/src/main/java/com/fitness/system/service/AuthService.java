package com.fitness.system.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fitness.common.api.ResultCode;
import com.fitness.common.exception.BizException;
import com.fitness.common.security.JwtUtil;
import com.fitness.system.dto.LoginRequest;
import com.fitness.system.dto.RegisterRequest;
import com.fitness.system.entity.SysUser;
import com.fitness.system.entity.UserProfile;
import com.fitness.system.mapper.SysUserMapper;
import com.fitness.system.mapper.UserProfileMapper;
import com.fitness.system.vo.LoginResponse;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

/**
 * 认证服务：注册、登录（JWT 签发）、登出（token 黑名单）
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final SysUserMapper sysUserMapper;
    private final UserProfileMapper userProfileMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final StringRedisTemplate redisTemplate;

    /**
     * 注册新用户：用户名唯一性校验（含并发兜底）、bcrypt 加密密码、插入默认个人资料
     */
    @Transactional
    public SysUser register(RegisterRequest req) {
        Long count = sysUserMapper.selectCount(
                Wrappers.<SysUser>lambdaQuery().eq(SysUser::getUsername, req.getUsername()));
        if (count > 0) {
            throw new BizException(ResultCode.CONFLICT, "用户名已被占用");
        }
        SysUser user = new SysUser();
        user.setUsername(req.getUsername());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setNickname(req.getNickname() == null || req.getNickname().isBlank()
                ? req.getUsername() : req.getNickname());
        user.setRole("USER");
        user.setStatus(1);
        try {
            sysUserMapper.insert(user);
        } catch (DuplicateKeyException e) {
            // 并发注册兜底：唯一约束兜住 selectCount 的时间窗
            throw new BizException(ResultCode.CONFLICT, "用户名已被占用");
        }

        UserProfile profile = new UserProfile();
        profile.setUserId(user.getId());
        userProfileMapper.insert(profile);
        return user;
    }

    /**
     * 登录：校验用户名/密码与账号状态，签发 JWT（含 jti）
     */
    public LoginResponse login(LoginRequest req) {
        SysUser user = sysUserMapper.selectOne(
                Wrappers.<SysUser>lambdaQuery().eq(SysUser::getUsername, req.getUsername()));
        if (user == null || !passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new BizException(ResultCode.UNAUTHORIZED, "用户名或密码错误");
        }
        if (user.getStatus() != null && user.getStatus() != 1) {
            throw new BizException(ResultCode.FORBIDDEN, "账号已被禁用");
        }
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        return new LoginResponse(token, user);
    }

    /**
     * 登出：将 token 的 jti 加入 Redis 黑名单（TTL=剩余有效期），token 立即失效
     */
    public void logout(String token) {
        if (token == null || token.isBlank()) {
            return;
        }
        try {
            Claims claims = jwtUtil.parseToken(token);
            redisTemplate.opsForValue().set("auth:blacklist:" + claims.getId(), "1",
                    jwtUtil.getRemainingSeconds(claims), TimeUnit.SECONDS);
        } catch (Exception ignored) {
            // token 已失效则无需加入黑名单
        }
    }
}
