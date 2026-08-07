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

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public Response<SysUser> register(@Valid @RequestBody RegisterRequest req) {
        return Response.ok(authService.register(req));
    }

    @PostMapping("/login")
    public Response<LoginResponse> login(@Valid @RequestBody LoginRequest req) {
        return Response.ok(authService.login(req));
    }

    @PostMapping("/logout")
    public Response<Void> logout(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        String token = header != null && header.startsWith("Bearer ")
                ? header.substring(7) : null;
        authService.logout(token);
        return Response.ok();
    }
}
