package com.fitness.training.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fitness.training.entity.TrainingRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

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
}
