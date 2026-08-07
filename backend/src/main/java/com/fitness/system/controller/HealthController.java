package com.fitness.system.controller;

import com.fitness.common.api.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 健康检查接口：探测数据库与 Redis 连通性（无需登录）
 */
@RestController
@RequiredArgsConstructor
public class HealthController {

    private final JdbcTemplate jdbcTemplate;
    private final StringRedisTemplate redisTemplate;

    /**
     * 返回 { db: up/down, redis: up/down }，供前端展示服务健康状态
     */
    @GetMapping("/api/health")
    public Response<Map<String, Object>> health() {
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
        return Response.ok(status);
    }
}
