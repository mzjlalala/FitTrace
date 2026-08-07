package com.fitness.diet.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.common.api.ResultCode;
import com.fitness.common.exception.BizException;
import com.fitness.diet.dto.DietRecordCreateRequest;
import com.fitness.diet.entity.DietFood;
import com.fitness.diet.entity.DietRecord;
import com.fitness.diet.mapper.DietFoodMapper;
import com.fitness.diet.mapper.DietRecordMapper;
import com.fitness.diet.vo.DietFoodVO;
import com.fitness.diet.vo.DietRecordVO;
import com.fitness.diet.vo.DietSummaryVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 饮食服务：食物库查询、饮食记录 CRUD、每日营养汇总
 */
@Service
@RequiredArgsConstructor
public class DietService {

    private final DietFoodMapper dietFoodMapper;
    private final DietRecordMapper dietRecordMapper;

    /**
     * 分页查询上架食物：支持名称关键字与分类筛选，按 id 升序
     */
    public IPage<DietFoodVO> listFoods(String keyword, String category, long page, long size) {
        Page<DietFood> p = new Page<>(page, size);
        dietFoodMapper.selectPage(p, Wrappers.<DietFood>lambdaQuery()
                .eq(DietFood::getStatus, 1)
                .like(keyword != null && !keyword.isBlank(), DietFood::getName, keyword)
                .eq(category != null && !category.isBlank(), DietFood::getCategory, category)
                .orderByAsc(DietFood::getId));
        Page<DietFoodVO> result = new Page<>(p.getCurrent(), p.getSize(), p.getTotal());
        result.setRecords(p.getRecords().stream().map(DietFoodVO::of).toList());
        return result;
    }

    /**
     * 获取食物详情；不存在或已下架抛 404 业务异常
     */
    public DietFoodVO getFood(Long id) {
        return DietFoodVO.of(requireFood(id));
    }

    /**
     * 创建饮食记录：校验食物存在性，插入后返回带营养换算值的详情
     */
    @Transactional
    public DietRecordVO create(Long userId, DietRecordCreateRequest req) {
        DietFood food = requireFood(req.getFoodId());
        DietRecord record = new DietRecord();
        record.setUserId(userId);
        record.setRecordDate(req.getRecordDate());
        record.setMealType(req.getMealType());
        record.setFoodId(req.getFoodId());
        record.setQuantityG(req.getQuantityG());
        dietRecordMapper.insert(record);
        return DietRecordVO.of(record, food);
    }

    /**
     * 指定日期的饮食记录（按创建时间升序，含食物名与营养换算值）
     */
    public List<DietRecordVO> listByDate(Long userId, LocalDate date) {
        List<DietRecord> records = dietRecordMapper.selectList(
                Wrappers.<DietRecord>lambdaQuery()
                        .eq(DietRecord::getUserId, userId)
                        .eq(DietRecord::getRecordDate, date)
                        .orderByAsc(DietRecord::getId));
        return toVOs(records);
    }

    /**
     * 更新饮食记录（整体替换字段）；不存在或非本人抛 404
     */
    @Transactional
    public DietRecordVO update(Long userId, Long id, DietRecordCreateRequest req) {
        DietRecord record = requireOwned(userId, id);
        DietFood food = requireFood(req.getFoodId());
        record.setRecordDate(req.getRecordDate());
        record.setMealType(req.getMealType());
        record.setFoodId(req.getFoodId());
        record.setQuantityG(req.getQuantityG());
        dietRecordMapper.updateById(record);
        return DietRecordVO.of(record, food);
    }

    /**
     * 删除饮食记录；不存在或非本人抛 404
     */
    @Transactional
    public void delete(Long userId, Long id) {
        requireOwned(userId, id);
        dietRecordMapper.deleteById(id);
    }

    /**
     * 每日营养汇总：范围内每天一条（按食物换算求和，保留 1 位），无记录的天补 0；
     * 起始日期晚于结束日期时返回空列表
     */
    public List<DietSummaryVO> summary(Long userId, LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            return List.of();
        }
        Map<LocalDate, DietRecordMapper.SummaryRow> rows = dietRecordMapper
                .selectSummary(userId, startDate, endDate).stream()
                .collect(Collectors.toMap(DietRecordMapper.SummaryRow::getRecordDate, r -> r));
        return startDate.datesUntil(endDate.plusDays(1)).map(d -> {
            DietSummaryVO vo = new DietSummaryVO();
            vo.setDate(d);
            DietRecordMapper.SummaryRow row = rows.get(d);
            vo.setCaloriesKcal(row == null ? BigDecimal.ZERO.setScale(1) : row.getCaloriesKcal());
            vo.setProteinG(row == null ? BigDecimal.ZERO.setScale(1) : row.getProteinG());
            vo.setFatG(row == null ? BigDecimal.ZERO.setScale(1) : row.getFatG());
            vo.setCarbG(row == null ? BigDecimal.ZERO.setScale(1) : row.getCarbG());
            return vo;
        }).toList();
    }

    /**
     * 按 id 校验食物存在且上架
     */
    private DietFood requireFood(Long id) {
        DietFood food = dietFoodMapper.selectById(id);
        if (food == null || food.getStatus() == null || food.getStatus() != 1) {
            throw new BizException(ResultCode.NOT_FOUND, "食物不存在");
        }
        return food;
    }

    /**
     * 按 id 校验记录归属：不存在或非本人抛 404（不泄露他人数据存在性）
     */
    private DietRecord requireOwned(Long userId, Long id) {
        DietRecord record = dietRecordMapper.selectById(id);
        if (record == null || !record.getUserId().equals(userId)) {
            throw new BizException(ResultCode.NOT_FOUND, "饮食记录不存在");
        }
        return record;
    }

    /**
     * 记录列表批量补食物信息并换算营养
     */
    private List<DietRecordVO> toVOs(List<DietRecord> records) {
        if (records.isEmpty()) {
            return List.of();
        }
        List<Long> foodIds = records.stream().map(DietRecord::getFoodId).distinct().toList();
        Map<Long, DietFood> foodsById = dietFoodMapper.selectBatchIds(foodIds).stream()
                .collect(Collectors.toMap(DietFood::getId, f -> f));
        return records.stream()
                .map(r -> DietRecordVO.of(r, foodsById.get(r.getFoodId())))
                .toList();
    }
}
