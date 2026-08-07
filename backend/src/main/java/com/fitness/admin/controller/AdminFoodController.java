package com.fitness.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fitness.admin.dto.AdminFoodQueryRequest;
import com.fitness.admin.dto.AdminFoodRequest;
import com.fitness.admin.service.AdminFoodService;
import com.fitness.admin.vo.AdminFoodVO;
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
 * 管理后台食物接口：全量列表（含下架）/ 创建 / 编辑 / 下架
 */
@RestController
@RequestMapping("/api/admin/foods")
@RequiredArgsConstructor
public class AdminFoodController {

    private final AdminFoodService adminFoodService;

    /**
     * 全量分页列表（POST body 传参，含下架数据），支持名称关键字与分类筛选
     */
    @PostMapping("/query")
    public Response<IPage<AdminFoodVO>> list(@RequestBody AdminFoodQueryRequest req) {
        long page = req.getPage() == null ? 1 : req.getPage();
        long size = req.getSize() == null ? 10 : req.getSize();
        return Response.ok(adminFoodService.list(req.getKeyword(), req.getCategory(), page, size));
    }

    /**
     * 新建食物（每 100g 营养数据）
     */
    @PostMapping
    public Response<AdminFoodVO> create(@Valid @RequestBody AdminFoodRequest req) {
        return Response.ok(adminFoodService.create(req));
    }

    /**
     * 编辑食物（可改 status 实现上架/下架）
     */
    @PutMapping("/{id}")
    public Response<AdminFoodVO> update(@PathVariable Long id,
                                        @Valid @RequestBody AdminFoodRequest req) {
        return Response.ok(adminFoodService.update(id, req));
    }

    /**
     * 下架食物（软删除：status=0，前台不可见，可再次上架）
     */
    @DeleteMapping("/{id}")
    public Response<Void> delete(@PathVariable Long id) {
        adminFoodService.delete(id);
        return Response.ok(null);
    }
}
