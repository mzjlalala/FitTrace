package com.fitness.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fitness.admin.dto.AdminPlanQueryRequest;
import com.fitness.admin.dto.AdminPlanRequest;
import com.fitness.admin.service.AdminPlanService;
import com.fitness.admin.vo.AdminPlanVO;
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
 * 管理后台计划接口：全量列表（含下架）/ 计划树创建 / 整体替换 / 下架
 */
@RestController
@RequestMapping("/api/admin/plans")
@RequiredArgsConstructor
public class AdminPlanController {

    private final AdminPlanService adminPlanService;

    /**
     * 全量分页列表（POST body 传参，含下架数据），支持名称关键字与目标筛选
     */
    @PostMapping("/query")
    public Response<IPage<AdminPlanVO>> list(@RequestBody AdminPlanQueryRequest req) {
        long page = req.getPage() == null ? 1 : req.getPage();
        long size = req.getSize() == null ? 10 : req.getSize();
        return Response.ok(adminPlanService.list(req.getKeyword(), req.getGoal(), page, size));
    }

    /**
     * 新建计划（周/日/动作树整体提交）
     */
    @PostMapping
    public Response<AdminPlanVO> create(@Valid @RequestBody AdminPlanRequest req) {
        return Response.ok(adminPlanService.create(req));
    }

    /**
     * 整体替换计划编排；该计划已有训练记录时返回 409
     */
    @PutMapping("/{id}")
    public Response<AdminPlanVO> update(@PathVariable Long id,
                                        @Valid @RequestBody AdminPlanRequest req) {
        return Response.ok(adminPlanService.update(id, req));
    }

    /**
     * 下架计划（软删除：status=0，前台不可见，树保留）
     */
    @DeleteMapping("/{id}")
    public Response<Void> delete(@PathVariable Long id) {
        adminPlanService.delete(id);
        return Response.ok(null);
    }
}
