package com.fitness.oss;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * OSS 客户端配置：oss.enabled=true 时创建 OSSClient，否则容器中无 OSS bean
 * （上传接口会返回「OSS 服务未配置」提示，避免启动即依赖真实账号）
 */
@Configuration
@EnableConfigurationProperties(OssProperties.class)
@RequiredArgsConstructor
public class OssConfig {

    /**
     * 创建 OSS 客户端（仅 oss.enabled=true 时生效）；
     * 配置项缺失时给出明确报错，避免客户端抛难以理解的异常
     */
    @Bean
    @ConditionalOnProperty(prefix = "oss", name = "enabled", havingValue = "true")
    public OSS ossClient(OssProperties props) {
        List<String> missing = new ArrayList<>();
        if (isBlank(props.getEndpoint())) missing.add("endpoint");
        if (isBlank(props.getAccessKeyId())) missing.add("access-key-id");
        if (isBlank(props.getAccessKeySecret())) missing.add("access-key-secret");
        if (isBlank(props.getBucket())) missing.add("bucket");
        if (isBlank(props.getUrlPrefix())) missing.add("url-prefix");
        if (!missing.isEmpty()) {
            throw new IllegalStateException(
                    "OSS 配置缺失，请完善 application.yml 的 oss." + String.join(", oss.", missing)
                            + "（或先保持 oss.enabled=false）");
        }
        return new OSSClientBuilder()
                .build(props.getEndpoint(), props.getAccessKeyId(), props.getAccessKeySecret());
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
