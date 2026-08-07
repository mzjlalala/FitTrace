package com.fitness.oss;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 阿里云 OSS 配置（application.yml 中 oss.* 段）
 */
@Data
@ConfigurationProperties(prefix = "oss")
public class OssProperties {

    /** 是否启用 OSS（填入下方配置后改为 true；未启用时上传接口返回提示） */
    private boolean enabled;

    /** 地域节点，如 oss-cn-hangzhou.aliyuncs.com（需与 bucket 同地域） */
    private String endpoint;

    /** AccessKey ID（建议使用 RAM 子账号） */
    private String accessKeyId;

    /** AccessKey Secret */
    private String accessKeySecret;

    /** Bucket 名称 */
    private String bucket;

    /** 图片访问 URL 前缀（含协议），如 https://fit-images.oss-cn-hangzhou.aliyuncs.com */
    private String urlPrefix;
}
