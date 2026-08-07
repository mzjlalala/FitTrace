package com.fitness.system.controller;

import com.fitness.common.api.Response;
import com.fitness.system.dto.LoginRequest;
import com.fitness.system.dto.RegisterRequest;
import com.fitness.system.entity.SysUser;
import com.fitness.system.service.AuthService;
import com.fitness.system.vo.LoginResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证接口：注册 / 登录 / 登出
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 注册新用户（用户名唯一，注册后自动创建默认个人资料）
     */
    @PostMapping("/register")
    public Response<SysUser> register(@Valid @RequestBody RegisterRequest req) {
        return Response.ok(authService.register(req));
    }

    /**
     * 登录，成功后返回 JWT token 与用户信息
     */
    @PostMapping("/login")
    public Response<LoginResponse> login(@Valid @RequestBody LoginRequest req) {
        return Response.ok(authService.login(req));
    }

    /**
     * 登出：将当前 token 的 jti 加入 Redis 黑名单，使其立即失效
     */
    @PostMapping("/logout")
    public Response<Void> logout(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        String token = header != null && header.startsWith("Bearer ")
                ? header.substring(7) : null;
        authService.logout(token);
        return Response.ok();
    }
}
