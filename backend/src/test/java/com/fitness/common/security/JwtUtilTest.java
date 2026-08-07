package com.fitness.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 注入真实 JwtUtil bean，保证测试与 application.yml 中的密钥一致。
 */
@ActiveProfiles("test")
@SpringBootTest
class JwtUtilTest {

    @Autowired
    private JwtUtil jwtUtil;

    @Test
    void generateAndParse_roundTrip() {
        String token = jwtUtil.generateToken(42L, "alice", "USER");
        Claims claims = jwtUtil.parseToken(token);
        assertThat(claims.getSubject()).isEqualTo("42");
        assertThat(claims.get("username", String.class)).isEqualTo("alice");
        assertThat(claims.getId()).isNotBlank();
    }

    @Test
    void expiredToken_throwsExpiredJwtException() {
        JwtUtil shortLived = new JwtUtil(jwtUtil.getSecret(), -10);
        String token = shortLived.generateToken(1L, "x", "USER");
        assertThatThrownBy(() -> shortLived.parseToken(token))
                .isInstanceOf(ExpiredJwtException.class);
    }

    @Test
    void tamperedPayload_throwsJwtException() {
        // 篡改 payload 段（真实篡改场景）必须被签名校验拦截
        String token = jwtUtil.generateToken(1L, "x", "USER");
        String[] parts = token.split("\\.");
        String tampered = parts[0] + "." + parts[1] + "x" + "." + parts[2];
        assertThatThrownBy(() -> jwtUtil.parseToken(tampered))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void remainingSeconds_isPositive() {
        String token = jwtUtil.generateToken(1L, "x", "USER");
        assertThat(jwtUtil.getRemainingSeconds(jwtUtil.parseToken(token))).isPositive();
    }
}
