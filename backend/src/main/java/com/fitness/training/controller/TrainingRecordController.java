package com.fitness.training.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fitness.common.api.Response;
import com.fitness.training.dto.TrainingRecordCreateRequest;
import com.fitness.training.service.TrainingRecordService;
import com.fitness.training.vo.TrainingRecordDetailVO;
import com.fitness.training.vo.TrainingRecordVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/**
 * 训练记录接口：创建 / 历史列表 / 详情 / 更新 / 删除
 */
@RestController
@RequestMapping("/api/training-records")
@RequiredArgsConstructor
public class TrainingRecordController {

    private final TrainingRecordService trainingRecordService;

    /**
     * 创建训练记录（记录 + 组数据一体提交，关联计划/动作需真实存在）
     */
    @PostMapping
    public Response<TrainingRecordDetailVO> create(@AuthenticationPrincipal Long userId,
                                                   @Valid @RequestBody TrainingRecordCreateRequest req) {
        return Response.ok(trainingRecordService.create(userId, req));
    }

    /**
     * 我的训练历史：分页，按训练日期倒序，可选日期范围筛选
     */
    @GetMapping
    public Response<IPage<TrainingRecordVO>> listMine(
            @AuthenticationPrincipal Long userId,
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return Response.ok(trainingRecordService.listMine(userId, startDate, endDate, page, size));
    }

    /**
     * 训练记录详情（含组数据，动作带名称）；不存在或非本人返回 404
     */
    @GetMapping("/{id}")
    public Response<TrainingRecordDetailVO> detail(@AuthenticationPrincipal Long userId,
                                                   @PathVariable Long id) {
        return Response.ok(trainingRecordService.getDetail(userId, id));
    }

    /**
     * 更新训练记录：整体替换（记录字段 + 组数据全部重写），仅本人可操作
     */
    @PutMapping("/{id}")
    public Response<TrainingRecordDetailVO> update(@AuthenticationPrincipal Long userId,
                                                   @PathVariable Long id,
                                                   @Valid @RequestBody TrainingRecordCreateRequest req) {
        return Response.ok(trainingRecordService.update(userId, id, req));
    }

    /**
     * 删除训练记录（级联删除组数据），仅本人可操作
     */
    @DeleteMapping("/{id}")
    public Response<Void> delete(@AuthenticationPrincipal Long userId, @PathVariable Long id) {
        trainingRecordService.delete(userId, id);
        return Response.ok(null);
    }
}
