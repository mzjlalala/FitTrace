package com.fitness.diet.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fitness.common.api.Response;
import com.fitness.diet.dto.DietRecordCreateRequest;
import com.fitness.diet.service.DietService;
import com.fitness.diet.vo.DietFoodVO;
import com.fitness.diet.vo.DietRecordVO;
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
import java.util.List;

/**
 * 饮食接口：食物库查询 / 饮食记录 CRUD / 每日营养汇总
 */
@RestController
@RequestMapping("/api/diet")
@RequiredArgsConstructor
public class DietController {

    private final DietService dietService;

    /**
     * 食物库分页查询：支持名称关键字与分类筛选
     */
    @GetMapping("/foods")
    public Response<IPage<DietFoodVO>> listFoods(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "12") long size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category) {
        return Response.ok(dietService.listFoods(keyword, category, page, size));
    }

    /**
     * 食物详情（每 100g 营养数据）；不存在或已下架返回 404
     */
    @GetMapping("/foods/{id}")
    public Response<DietFoodVO> food(@PathVariable Long id) {
        return Response.ok(dietService.getFood(id));
    }

    /**
     * 记录一笔饮食：日期 + 餐次 + 食物 + 食用克数，返回营养换算值
     */
    @PostMapping("/records")
    public Response<DietRecordVO> create(@AuthenticationPrincipal Long userId,
                                         @Valid @RequestBody DietRecordCreateRequest req) {
        return Response.ok(dietService.create(userId, req));
    }

    /**
     * 指定日期的饮食记录（含食物名与营养换算值，按创建时间升序）
     */
    @GetMapping("/records")
    public Response<List<DietRecordVO>> listByDate(
            @AuthenticationPrincipal Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return Response.ok(dietService.listByDate(userId, date));
    }

    /**
     * 更新一笔饮食记录（整体替换），仅本人可操作
     */
    @PutMapping("/records/{id}")
    public Response<DietRecordVO> update(@AuthenticationPrincipal Long userId,
                                         @PathVariable Long id,
                                         @Valid @RequestBody DietRecordCreateRequest req) {
        return Response.ok(dietService.update(userId, id, req));
    }

    /**
     * 删除一笔饮食记录，仅本人可操作
     */
    @DeleteMapping("/records/{id}")
    public Response<Void> delete(@AuthenticationPrincipal Long userId, @PathVariable Long id) {
        dietService.delete(userId, id);
        return Response.ok(null);
    }
}
