package com.fitness.system.controller;

import com.fitness.common.security.JwtUtil;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 帮助方法：注册并返回该用户的登录 token。
 */
@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private StringRedisTemplate redisTemplate;

    private String registerAndLogin(String username) throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"pass123\"}"))
                .andExpect(jsonPath("$.code").value(200));
        return login(username);
    }

    private String login(String username) throws Exception {
        String body = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"pass123\"}"))
                .andExpect(jsonPath("$.code").value(200))
                .andReturn().getResponse().getContentAsString();
        return body.replaceAll(".*\"token\":\"([^\"]+)\".*", "$1");
    }

    @Test
    void getProfile_returnsUserWithDefaults() throws Exception {
        String token = registerAndLogin("profile1");
        mockMvc.perform(get("/api/user/profile").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.username").value("profile1"))
                .andExpect(jsonPath("$.data.nickname").value("profile1"))
                .andExpect(jsonPath("$.data.password").doesNotExist())
                .andExpect(jsonPath("$.data.heightCm").doesNotExist());
    }

    @Test
    void updateProfile_thenGetReflectsChanges() throws Exception {
        String token = registerAndLogin("profile2");
        mockMvc.perform(put("/api/user/profile")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nickname\":\"大壮\",\"gender\":\"MALE\",\"heightCm\":180.0,\"weightKg\":80.5,\"goal\":\"MUSCLE_GAIN\",\"fitnessLevel\":\"INTERMEDIATE\",\"weeklyFrequency\":4}"))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.nickname").value("大壮"))
                .andExpect(jsonPath("$.data.heightCm").value(180.0))
                .andExpect(jsonPath("$.data.goal").value("MUSCLE_GAIN"));

        mockMvc.perform(get("/api/user/profile").header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.data.nickname").value("大壮"))
                .andExpect(jsonPath("$.data.weeklyFrequency").value(4));
    }

    @Test
    void updateProfile_updatesAvatar() throws Exception {
        String token = registerAndLogin("avatar1");
        String avatarUrl = "https://fit-test.oss-cn-hangzhou.aliyuncs.com/fitness/20260807/abc.png";
        mockMvc.perform(put("/api/user/profile")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"avatar\":\"" + avatarUrl + "\"}"))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.avatar").value(avatarUrl));

        mockMvc.perform(get("/api/user/profile").header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.data.avatar").value(avatarUrl));
    }

    @Test
    void getProfile_withoutToken_returns401() throws Exception {
        mockMvc.perform(get("/api/user/profile"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    void logout_blacklistsToken() throws Exception {
        String token = registerAndLogin("logout1");
        mockMvc.perform(post("/api/auth/logout").header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.code").value(200));

        mockMvc.perform(get("/api/user/profile").header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));

        // 清理 Redis 黑名单键，避免影响其他测试
        Claims claims = jwtUtil.parseToken(token);
        redisTemplate.delete("auth:blacklist:" + claims.getId());
    }

    @Test
    void logout_thenReLogin_works() throws Exception {
        String token = registerAndLogin("logout2");
        mockMvc.perform(post("/api/auth/logout").header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.code").value(200));

        // 重新登录应得到新 token，且可用
        String newToken = login("logout2");
        mockMvc.perform(get("/api/user/profile").header("Authorization", "Bearer " + newToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}
