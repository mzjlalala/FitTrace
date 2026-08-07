package com.fitness.training.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fitness.training.entity.TrainingRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 训练记录 Mapper
 */
@Mapper
public interface TrainingRecordMapper extends BaseMapper<TrainingRecord> {
}
