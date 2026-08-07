package com.fitness.oss;

import com.aliyun.oss.OSS;
import com.fitness.common.api.ResultCode;
import com.fitness.common.exception.BizException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/**
 * OSS 图片上传服务：校验类型与大小，上传后返回可访问 URL。
 * 对象 key 形如 fitness/yyyyMMdd/uuid.ext，按日期归档便于管理。
 */
@Service
@RequiredArgsConstructor
public class OssService {

    /** 允许上传的图片扩展名 */
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp", "gif");

    /** 单张图片大小上限：5MB */
    private static final long MAX_SIZE_BYTES = 5 * 1024 * 1024L;

    private final ObjectProvider<OSS> ossProvider;
    private final OssProperties props;

    /**
     * 上传图片到 OSS，返回可访问 URL。
     *
     * @param file 图片文件（jpg/jpeg/png/webp/gif，≤5MB）
     * @return 图片 URL
     * @throws BizException 未启用 OSS / 格式不支持 / 超过大小限制 / 上传失败
     */
    public String uploadImage(MultipartFile file) {
        OSS oss = ossProvider.getIfAvailable();
        if (oss == null) {
            throw new BizException(ResultCode.BAD_REQUEST, "OSS 服务未配置，请联系管理员");
        }
        String ext = extensionOf(file.getOriginalFilename());
        if (!ALLOWED_EXTENSIONS.contains(ext)) {
            throw new BizException(ResultCode.BAD_REQUEST, "仅支持 jpg / png / webp / gif 格式图片");
        }
        if (file.getSize() > MAX_SIZE_BYTES) {
            throw new BizException(ResultCode.BAD_REQUEST, "图片大小不能超过 5MB");
        }
        String key = "fitness/"
                + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE)
                + "/" + UUID.randomUUID().toString().replace("-", "") + "." + ext;
        try (InputStream in = file.getInputStream()) {
            oss.putObject(props.getBucket(), key, in);
        } catch (IOException e) {
            throw new BizException(ResultCode.INTERNAL_ERROR, "图片上传失败");
        }
        return props.getUrlPrefix() + "/" + key;
    }

    /**
     * 提取文件名扩展名（小写，无扩展名返回空串）
     */
    private String extensionOf(String filename) {
        if (filename == null) {
            return "";
        }
        int dot = filename.lastIndexOf('.');
        return dot < 0 ? "" : filename.substring(dot + 1).toLowerCase(Locale.ROOT);
    }
}
