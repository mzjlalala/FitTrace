package com.fitness.oss;

import com.fitness.common.api.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * OSS 上传接口：登录用户可上传图片（动作封面、食物图片、用户头像共用），返回 OSS 可访问 URL。
 * 引用环节（如动作/食物入库、头像保存）各自带权限校验。
 */
@RestController
@RequestMapping("/api/oss")
@RequiredArgsConstructor
public class OssController {

    private final OssService ossService;

    /**
     * 上传图片（jpg/png/webp/gif，≤5MB）
     *
     * @param file multipart 文件，字段名为 file
     * @return 图片 URL
     */
    @PostMapping("/upload")
    public Response<String> upload(@RequestParam("file") MultipartFile file) {
        return Response.ok(ossService.uploadImage(file));
    }
}
