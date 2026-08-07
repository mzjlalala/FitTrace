package com.fitness.system.controller;

import com.fitness.common.api.R;
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

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserProfileService userProfileService;

    @GetMapping("/profile")
    public R<UserInfoVO> getProfile(@AuthenticationPrincipal Long userId) {
        return R.ok(userProfileService.getProfile(userId));
    }

    @PutMapping("/profile")
    public R<UserInfoVO> updateProfile(@AuthenticationPrincipal Long userId,
                                       @Valid @RequestBody UserProfileUpdateRequest req) {
        return R.ok(userProfileService.updateProfile(userId, req));
    }
}
