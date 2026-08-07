package com.fitness.training.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fitness.training.entity.TrainingRecord;
import lombok.Data;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 训练记录 Mapper
 */
@Mapper
public interface TrainingRecordMapper extends BaseMapper<TrainingRecord> {

    /**
     * 统计总次数、总时长（空值按 0）、打卡天数（按日期去重）
     */
    @Select("""
            SELECT COUNT(*)        AS total,
                   COALESCE(SUM(duration_minutes), 0) AS minutes,
                   COUNT(DISTINCT training_date)      AS days
            FROM training_record
            WHERE user_id = #{userId}
            """)
    Map<String, Object> selectSummary(@Param("userId") Long userId);

    /**
     * 热力图数据：某日期之后每天的记录条数（无记录的天不返回，由服务层补 0）
     */
    @Select("""
            SELECT training_date AS trainingDate, COUNT(*) AS count
            FROM training_record
            WHERE user_id = #{userId} AND training_date >= #{from}
            GROUP BY training_date
            ORDER BY training_date
            """)
    List<HeatmapRow> selectHeatmap(@Param("userId") Long userId, @Param("from") LocalDate from);

    /**
     * 热力图查询行（某天的记录条数）
     */
    @Data
    class HeatmapRow {
        private LocalDate trainingDate;
        private Long count;
    }
}
