package com.fitness.training.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fitness.training.entity.TrainingRecordSet;
import lombok.Data;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 训练组数据 Mapper
 */
@Mapper
public interface TrainingRecordSetMapper extends BaseMapper<TrainingRecordSet> {

    /**
     * 每个动作的最佳组（PR）：仅统计完成的组且重量大于 0，
     * 同动作按重量降序、同重量按次数降序、再按日期降序取第一条（PG DISTINCT ON 需配套 ORDER BY 前缀）
     */
    @Select("""
            SELECT DISTINCT ON (s.action_id) s.action_id       AS actionId,
                                             s.weight_kg       AS weightKg,
                                             s.reps            AS reps,
                                             r.training_date   AS recordDate
            FROM training_record_set s
            JOIN training_record r ON r.id = s.record_id
            WHERE r.user_id = #{userId} AND s.done_flag = TRUE AND s.weight_kg > 0
            ORDER BY s.action_id, s.weight_kg DESC, s.reps DESC, r.training_date DESC
            """)
    List<PrRow> selectPrRows(@Param("userId") Long userId);

    /**
     * PR 查询行（每个动作的最佳组）
     */
    @Data
    class PrRow {
        private Long actionId;
        private BigDecimal weightKg;
        private Integer reps;
        private LocalDate recordDate;
    }
}
