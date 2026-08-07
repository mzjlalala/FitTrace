package com.fitness.common.security;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.concurrent.TimeUnit;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 使用 /api/not-exist 作为"受保护但未实现"的探针路径：
 * 无 token → HTTP 401（未认证）；有有效 token → 已过认证进入路由，资源不存在
 * → 全局约定业务错误返回 HTTP 200 + 业务 code 404。
 */
@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class SecurityFilterTest {

    private static final String PROTECTED_PROBE = "/api/not-exist";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private StringRedisTemplate redisTemplate;

    @Test
    void noToken_protectedApi_returns401() throws Exception {
        mockMvc.perform(get(PROTECTED_PROBE))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    void invalidToken_returns401() throws Exception {
        mockMvc.perform(get(PROTECTED_PROBE).header("Authorization", "Bearer abc.def.ghi"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    void validToken_passesSecurity_returns404Code() throws Exception {
        String token = jwtUtil.generateToken(1L, "tester", "USER");
        mockMvc.perform(get(PROTECTED_PROBE).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404));
    }

    @Test
    void blacklistedToken_returns401() throws Exception {
        String token = jwtUtil.generateToken(1L, "tester", "USER");
        Claims claims = jwtUtil.parseToken(token);
        String redisKey = "auth:blacklist:" + claims.getId();
        try {
            redisTemplate.opsForValue().set(redisKey, "1",
                    jwtUtil.getRemainingSeconds(claims), TimeUnit.SECONDS);
            mockMvc.perform(get(PROTECTED_PROBE).header("Authorization", "Bearer " + token))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.code").value(401));
        } finally {
            redisTemplate.delete(redisKey);
        }
    }
}
