package com.fitness.oss;

import com.aliyun.oss.OSS;
import com.fitness.common.api.ResultCode;
import com.fitness.common.exception.BizException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mock.web.MockMultipartFile;

import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * OSS 上传服务单元测试：mock OSS 客户端，校验格式/大小限制与 key 生成
 */
class OssServiceTest {

    private OSS ossClient;
    private ObjectProvider<OSS> provider;
    private OssProperties props;
    private OssService service;

    @BeforeEach
    void setUp() {
        ossClient = mock(OSS.class);
        provider = mock(ObjectProvider.class);
        props = new OssProperties();
        props.setBucket("fit-images");
        props.setUrlPrefix("https://fit-images.oss-cn-hangzhou.aliyuncs.com");
        when(provider.getIfAvailable()).thenReturn(ossClient);
        service = new OssService(provider, props);
    }

    @Test
    void uploadImage_rejectsNonImage() {
        MockMultipartFile file = new MockMultipartFile("file", "a.txt", "text/plain", "hello".getBytes());
        BizException ex = assertThrows(BizException.class, () -> service.uploadImage(file));
        assertEquals(ResultCode.BAD_REQUEST, ex.getResultCode());
    }

    @Test
    void uploadImage_rejectsOversize() {
        byte[] big = new byte[5 * 1024 * 1024 + 1];
        MockMultipartFile file = new MockMultipartFile("file", "a.png", "image/png", big);
        BizException ex = assertThrows(BizException.class, () -> service.uploadImage(file));
        assertEquals(ResultCode.BAD_REQUEST, ex.getResultCode());
    }

    @Test
    void uploadImage_returnsUrlWithGeneratedKey() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "a.PNG", "image/png", new byte[]{1, 2, 3});
        String url = service.uploadImage(file);

        assertTrue(url.startsWith("https://fit-images.oss-cn-hangzhou.aliyuncs.com/fitness/"));
        assertTrue(url.endsWith(".png"));
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        verify(ossClient).putObject(eq("fit-images"), keyCaptor.capture(), any(InputStream.class));
        assertTrue(keyCaptor.getValue().startsWith("fitness/"));
    }

    @Test
    void uploadImage_disabled_throws() {
        when(provider.getIfAvailable()).thenReturn(null);
        MockMultipartFile file = new MockMultipartFile("file", "a.png", "image/png", new byte[]{1});
        BizException ex = assertThrows(BizException.class, () -> service.uploadImage(file));
        assertEquals(ResultCode.BAD_REQUEST, ex.getResultCode());
    }
}
