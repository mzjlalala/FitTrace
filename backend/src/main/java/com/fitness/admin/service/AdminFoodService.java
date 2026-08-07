package com.fitness.admin.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.admin.dto.AdminFoodRequest;
import com.fitness.admin.vo.AdminFoodVO;
import com.fitness.common.api.ResultCode;
import com.fitness.common.exception.BizException;
import com.fitness.diet.entity.DietFood;
import com.fitness.diet.mapper.DietFoodMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * 管理后台食物服务：全量列表（含下架）、创建、编辑、下架/上架
 */
@Service
@RequiredArgsConstructor
public class AdminFoodService {

    private final DietFoodMapper dietFoodMapper;

    /**
     * 全量分页（含下架数据）：名称关键字/分类筛选，按 id 倒序
     */
    public IPage<AdminFoodVO> list(String keyword, String category, long page, long size) {
        Page<DietFood> p = new Page<>(page, size);
        dietFoodMapper.selectPage(p, Wrappers.<DietFood>lambdaQuery()
                .like(keyword != null && !keyword.isBlank(), DietFood::getName, keyword)
                .eq(category != null && !category.isBlank(), DietFood::getCategory, category)
                .orderByDesc(DietFood::getId));
        Page<AdminFoodVO> result = new Page<>(p.getCurrent(), p.getSize(), p.getTotal());
        result.setRecords(p.getRecords().stream().map(AdminFoodVO::of).toList());
        return result;
    }

    /**
     * 创建食物；不存在抛 404
     */
    @Transactional
    public AdminFoodVO create(AdminFoodRequest req) {
        DietFood food = new DietFood();
        apply(food, req);
        food.setStatus(req.getStatus() == null ? 1 : req.getStatus());
        dietFoodMapper.insert(food);
        return AdminFoodVO.of(food);
    }

    /**
     * 编辑食物（可改 status 实现上/下架）；不存在抛 404
     */
    @Transactional
    public AdminFoodVO update(Long id, AdminFoodRequest req) {
        DietFood food = requireFood(id);
        apply(food, req);
        dietFoodMapper.updateById(food);
        return AdminFoodVO.of(food);
    }

    /**
     * 下架食物（软删除 status=0）；不存在抛 404
     */
    @Transactional
    public void delete(Long id) {
        DietFood food = requireFood(id);
        food.setStatus(0);
        dietFoodMapper.updateById(food);
    }

    private DietFood requireFood(Long id) {
        DietFood food = dietFoodMapper.selectById(id);
        if (food == null) {
            throw new BizException(ResultCode.NOT_FOUND, "食物不存在");
        }
        return food;
    }

    private void apply(DietFood food, AdminFoodRequest req) {
        food.setName(req.getName());
        food.setCategory(req.getCategory());
        food.setCaloriesPer100g(req.getCaloriesPer100g());
        food.setProteinPer100g(req.getProteinPer100g() == null ? BigDecimal.ZERO : req.getProteinPer100g());
        food.setFatPer100g(req.getFatPer100g() == null ? BigDecimal.ZERO : req.getFatPer100g());
        food.setCarbPer100g(req.getCarbPer100g() == null ? BigDecimal.ZERO : req.getCarbPer100g());
        food.setImage(req.getImage());
        food.setStatus(req.getStatus());
    }
}
