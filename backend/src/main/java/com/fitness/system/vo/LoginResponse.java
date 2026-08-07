package com.fitness.system.vo;

import com.fitness.system.entity.SysUser;

/**
 * 登录响应：JWT 令牌 + 用户信息
 */
public record LoginResponse(
        /** JWT 访问令牌（Bearer 认证使用） */
        String token,
        /** 用户信息 */
        SysUser user) {
}
