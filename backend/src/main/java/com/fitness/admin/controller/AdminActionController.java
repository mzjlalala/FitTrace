package com.fitness.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fitness.admin.dto.AdminActionQueryRequest;
import com.fitness.admin.dto.AdminActionRequest;
import com.fitness.admin.service.AdminActionService;
import com.fitness.admin.vo.AdminActionVO;
import com.fitness.common.api.Response;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理后台动作接口：全量列表（含下架）/ 创建 / 编辑 / 下架
 */
@RestController
@RequestMapping("/api/admin/actions")
@RequiredArgsConstructor
public class AdminActionController {

    private final AdminActionService adminActionService;

    /**
     * 全量分页列表（POST body 传参，含下架数据），支持名称关键字与分类筛选
     */
    @PostMapping("/query")
    public Response<IPage<AdminActionVO>> list(@RequestBody AdminActionQueryRequest req) {
        long page = req.getPage() == null ? 1 : req.getPage();
        long size = req.getSize() == null ? 10 : req.getSize();
        return Response.ok(adminActionService.list(req.getKeyword(), req.getCategoryId(), page, size));
    }

    /**
     * 新建动作（步骤/技巧/注意事项为字符串数组，存 JSONB）
     */
    @PostMapping
    public Response<AdminActionVO> create(@Valid @RequestBody AdminActionRequest req) {
        return Response.ok(adminActionService.create(req));
    }

    /**
     * 编辑动作（可改 status 实现上架/下架）
     */
    @PutMapping("/{id}")
    public Response<AdminActionVO> update(@PathVariable Long id,
                                          @Valid @RequestBody AdminActionRequest req) {
        return Response.ok(adminActionService.update(id, req));
    }

    /**
     * 下架动作（软删除：status=0，前台不可见，可再次上架）
     */
    @DeleteMapping("/{id}")
    public Response<Void> delete(@PathVariable Long id) {
        adminActionService.delete(id);
        return Response.ok(null);
    }
}
