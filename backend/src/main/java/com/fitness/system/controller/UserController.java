package com.fitness.system.controller;

import com.fitness.common.api.Response;
import com.fitness.system.dto.UserProfileUpdateRequest;
import com.fitness.system.service.UserProfileService;
import com.fitness.system.vo.UserInfoVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户接口：个人信息与身体数据的查询与维护
 */
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserProfileService userProfileService;

    /**
     * 获取当前登录用户的个人信息与身体数据（资料缺失时自动初始化）
     */
    @GetMapping("/profile")
    public Response<UserInfoVO> getProfile(@AuthenticationPrincipal Long userId) {
        return Response.ok(userProfileService.getProfile(userId));
    }

    /**
     * 更新当前登录用户的个人信息与身体数据（整体提交，null 字段置空）
     */
    @PutMapping("/profile")
    public Response<UserInfoVO> updateProfile(@AuthenticationPrincipal Long userId,
                                       @Valid @RequestBody UserProfileUpdateRequest req) {
        return Response.ok(userProfileService.updateProfile(userId, req));
    }
}
