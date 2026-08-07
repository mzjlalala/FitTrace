package com.fitness.oss;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OSS 客户端配置：oss.enabled=true 时创建 OSSClient，否则容器中无 OSS bean
 * （上传接口会返回「OSS 服务未配置」提示，避免启动即依赖真实账号）
 */
@Configuration
@EnableConfigurationProperties(OssProperties.class)
@RequiredArgsConstructor
public class OssConfig {

    /**
     * 创建 OSS 客户端（仅 oss.enabled=true 时生效）
     */
    @Bean
    @ConditionalOnProperty(prefix = "oss", name = "enabled", havingValue = "true")
    public OSS ossClient(OssProperties props) {
        return new OSSClientBuilder()
                .build(props.getEndpoint(), props.getAccessKeyId(), props.getAccessKeySecret());
    }
}
