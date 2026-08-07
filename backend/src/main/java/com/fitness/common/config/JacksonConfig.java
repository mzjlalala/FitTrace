package com.fitness.common.config;

import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.format.DateTimeFormatter;

/**
 * Jackson 全局配置：LocalDateTime 统一按 yyyy-MM-dd HH:mm:ss 序列化/反序列化
 * （application.yml 的 spring.jackson.date-format 只对 java.util.Date 生效，
 * LocalDateTime 默认走 ISO 格式如 2026-08-07T14:58:04.301，前端展示不友好）
 */
@Configuration
public class JacksonConfig {

    private static final DateTimeFormatter DATETIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 注册 LocalDateTime 的全局序列化格式
     */
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer localDateTimeCustomizer() {
        return builder -> builder
                .serializers(new LocalDateTimeSerializer(DATETIME))
                .deserializers(new LocalDateTimeDeserializer(DATETIME));
    }
}
