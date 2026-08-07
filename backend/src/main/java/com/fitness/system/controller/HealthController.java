package com.fitness.system.controller;

import com.fitness.common.api.R;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class HealthController {

    private final JdbcTemplate jdbcTemplate;
    private final StringRedisTemplate redisTemplate;

    @GetMapping("/api/health")
    public R<Map<String, Object>> health() {
        boolean dbOk = false;
        boolean redisOk = false;
        try {
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            dbOk = true;
        } catch (Exception ignored) {
        }
        try (RedisConnection connection = redisTemplate.getConnectionFactory().getConnection()) {
            redisOk = "PONG".equalsIgnoreCase(new String(connection.ping()));
        } catch (Exception ignored) {
        }
        Map<String, Object> status = new LinkedHashMap<>();
        status.put("db", dbOk ? "up" : "down");
        status.put("redis", redisOk ? "up" : "down");
        return R.ok(status);
    }
}
