package com.fitness.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fitness.admin.dto.AdminUserQueryRequest;
import com.fitness.admin.dto.AdminUserStatusRequest;
import com.fitness.admin.service.AdminUserService;
import com.fitness.admin.vo.AdminUserVO;
import com.fitness.common.api.Response;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理后台用户接口：全量列表 / 禁用启用
 */
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;

    /**
     * 全量分页列表（POST body 传参，不含密码），支持用户名/昵称关键字筛选
     */
    @PostMapping("/query")
    public Response<IPage<AdminUserVO>> list(@RequestBody AdminUserQueryRequest req) {
        long page = req.getPage() == null ? 1 : req.getPage();
        long size = req.getSize() == null ? 10 : req.getSize();
        return Response.ok(adminUserService.list(req.getKeyword(), page, size));
    }

    /**
     * 禁用/启用用户（status 0/1）；禁用自己返回 409
     */
    @PutMapping("/{id}/status")
    public Response<AdminUserVO> updateStatus(@AuthenticationPrincipal Long operatorId,
                                              @PathVariable Long id,
                                              @Valid @RequestBody AdminUserStatusRequest req) {
        return Response.ok(adminUserService.updateStatus(operatorId, id, req.getStatus()));
    }
}
